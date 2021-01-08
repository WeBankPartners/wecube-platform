package com.webank.wecube.platform.workflow.delegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.instance.EventBasedGateway;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;

import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEventImpl;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.parse.SpringApplicationContextUtil;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusMapper;

public abstract class AbstractServiceTaskHandleDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("service processing dispatch,inst={},exec={},nodeId={},instKey={}",
                    execution.getProcessInstanceId(), execution.getId(), execution.getCurrentActivityId(),
                    execution.getProcessBusinessKey());
        }

        String processDefinitionId = execution.getProcessDefinitionId();

        ProcessDefinition procDef = execution.getProcessEngine().getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();

        if (procDef == null) {
            getLogger().warn("cannot find process definition in execution,executionId={}", execution.getId());
            throw new Exception("Execution errors.");
        }

        try {
            QueueHolder.putServiceInvocationEvent(serviceInvocationEvent(execution, procDef));
        } catch (Throwable e) {
            getLogger().warn("plugin invocation errors", e);
            throw e;
        }

        logServiceNodeExecution(execution);
    }

    protected void logServiceNodeExecution(DelegateExecution execution) {
        String activityId = execution.getCurrentActivityId();
        if (activityId == null) {
            return;
        }

        String nodeId = activityId;
        if (activityId.startsWith(WorkflowConstants.PREFIX_SRV_BEAN_SERVICETASK)) {
            nodeId = activityId.substring(WorkflowConstants.PREFIX_SRV_BEAN_SERVICETASK.length());
        }

        String procInstanceBizKey = execution.getProcessBusinessKey();

        ServiceNodeStatusMapper repository = SpringApplicationContextUtil
                .getBean(ServiceNodeStatusMapper.class);

        ServiceNodeStatusEntity entity = repository.selectOneByProcInstanceBizKeyAndNodeId(procInstanceBizKey, nodeId);

        if (entity != null) {
            entity.setTryTimes(entity.getTryTimes() + 1);
            entity.setStatus(TraceStatus.InProgress);
            entity.setUpdatedTime(new Date());
            entity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            repository.updateByPrimaryKeySelective(entity);
        }

    }

    private ServiceInvocationEvent serviceInvocationEvent(DelegateExecution execution, ProcessDefinition procDef) {
        ServiceInvocationEventImpl event = new ServiceInvocationEventImpl();

        event.setDefinitionId(execution.getProcessDefinitionId());
        event.setDefinitionVersion(procDef.getVersion());
        event.setDefinitionKey(procDef.getKey());

        // event.setExecutionId(execution.getId());
        event.setBusinessKey(execution.getProcessBusinessKey());
        event.setInstanceId(execution.getProcessInstanceId());

        event.setEventSourceId(execution.getCurrentActivityId());
        event.setEventSourceName(execution.getCurrentActivityName());

        event.setEventType(ServiceInvocationEvent.EventType.SERVICE_INVOCATION);

        ServiceTask serviceTask = execution.getBpmnModelInstance()
                .getModelElementById(execution.getCurrentActivityId());

        List<EventBasedGateway> eventBaseGateways = serviceTask.getSucceedingNodes()
                .filterByType(EventBasedGateway.class).list();
        EventBasedGateway eventBaseGateway = null;
        if (!eventBaseGateways.isEmpty()) {
            eventBaseGateway = eventBaseGateways.get(0);
        }
        IntermediateCatchEvent intermediateCatchEvent = null;
        if (eventBaseGateway != null) {

            List<IntermediateCatchEvent> intermediateCatchEvents = eventBaseGateway.getSucceedingNodes()
                    .filterByType(IntermediateCatchEvent.class).list();

            for (IntermediateCatchEvent ice : intermediateCatchEvents) {
                getLogger().debug("IntermediateCatchEvent:{} {}", ice.getId(), ice.getName());

                Collection<EventDefinition> eventDefinitions = ice.getEventDefinitions();

                for (EventDefinition ed : eventDefinitions) {
                    getLogger().debug("EventDefinition: {} {}", ed.getId(), ed.getElementType().getTypeName());
                    if ("signalEventDefinition".equals(ed.getElementType().getTypeName())) {
                        intermediateCatchEvent = ice;
                        break;
                    }
                }

                if (intermediateCatchEvent != null) {
                    break;
                }
            }

        }

        if (intermediateCatchEvent != null) {
            event.setExecutionId(intermediateCatchEvent.getId());
        }

        List<String> allowedOptions = tryCalAllowedOptions(execution);
        if (allowedOptions != null) {
            for (String option : allowedOptions) {
                event.addAllowedOption(option);
            }
        }

        return event;
    }

    protected List<String> tryCalAllowedOptions(DelegateExecution execution) {
        List<String> allowedOptions = new ArrayList<>();
        FlowElement flowElement = execution.getBpmnModelElementInstance();
        ModelElementInstance parentEi = flowElement.getParentElement();
        if (parentEi != null) {
            SubProcess subProcess = execution.getBpmnModelInstance()
                    .getModelElementById(parentEi.getAttributeValue("id"));

            List<ExclusiveGateway> exGws = subProcess.getSucceedingNodes().filterByType(ExclusiveGateway.class).list();
            if (exGws.size() == 1) {
                Collection<SequenceFlow> sfs = exGws.get(0).getOutgoing();
                if (!sfs.isEmpty()) {
                    for (SequenceFlow sf : sfs) {
                        getLogger().debug("add sequence name: {}", sf.getName());
                        allowedOptions.add(sf.getName());
                    }
                }
            }
        }

        return allowedOptions;
    }

    protected abstract Logger getLogger();
}
