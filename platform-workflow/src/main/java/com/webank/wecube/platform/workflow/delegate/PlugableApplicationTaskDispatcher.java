package com.webank.wecube.platform.workflow.delegate;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.instance.EventBasedGateway;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEventImpl;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.parse.SpringApplicationContextUtil;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusRepository;

/**
 * 
 * @author gavin
 *
 */
@Component("srvBean")
public class PlugableApplicationTaskDispatcher implements JavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(PlugableApplicationTaskDispatcher.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("service processing dispatch,inst={},exec={},nodeId={},instKey={}", execution.getProcessInstanceId(),
                    execution.getId(), execution.getCurrentActivityId(), execution.getProcessBusinessKey());
        }

        String processDefinitionId = execution.getProcessDefinitionId();

        ProcessDefinition procDef = execution.getProcessEngine().getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();

        if (procDef == null) {
            log.error("cannot find process definition in execution,executionId={}", execution.getId());
            throw new Exception("execution errors");
        }

        try {
            QueueHolder.putServiceInvocationEvent(serviceInvocationEvent(execution, procDef));
        } catch (Throwable e) {
            log.error("plugin invocation errors", e);
            throw e;
        }
        
        logServiceNodeExecution(execution);
    }
    
    protected void logServiceNodeExecution(DelegateExecution execution){
        String activityId = execution.getCurrentActivityId();
        if(activityId == null){
            return;
        }
        
        String nodeId = activityId;
        if(activityId.startsWith(WorkflowConstants.PREFIX_SRV_BEAN_SERVICETASK)){
            nodeId = activityId.substring(WorkflowConstants.PREFIX_SRV_BEAN_SERVICETASK.length());
        }
        
        String procInstanceBizKey = execution.getProcessBusinessKey();
        
        ServiceNodeStatusRepository repository = SpringApplicationContextUtil.getBean(ServiceNodeStatusRepository.class);
        
        ServiceNodeStatusEntity entity = repository.findOneByProcInstanceBizKeyAndNodeId(procInstanceBizKey, nodeId);
        
        if(entity != null){
            entity.setTryTimes(entity.getTryTimes() +  1);
            entity.setStatus(TraceStatus.InProgress);
            entity.setUpdatedTime(new Date());
            entity.setUpdatedBy("system");
            repository.save(entity);
        }
        
        
    }

    private ServiceInvocationEvent serviceInvocationEvent(DelegateExecution execution, ProcessDefinition procDef) {
        ServiceInvocationEventImpl event = new ServiceInvocationEventImpl();

        event.setDefinitionId(execution.getProcessDefinitionId());
        event.setDefinitionVersion(procDef.getVersion());
        event.setDefinitionKey(procDef.getKey());

//        event.setExecutionId(execution.getId());
        event.setBusinessKey(execution.getProcessBusinessKey());
        event.setInstanceId(execution.getProcessInstanceId());

        event.setEventSourceId(execution.getCurrentActivityId());
        event.setEventSourceName(execution.getCurrentActivityName());
        
        event.setEventType(ServiceInvocationEvent.EventType.SERVICE_INVOCATION);
        
        ServiceTask serviceTask = execution.getBpmnModelInstance()
                .getModelElementById(execution.getCurrentActivityId());

        List<EventBasedGateway> eventBaseGateways = serviceTask.getSucceedingNodes().filterByType(EventBasedGateway.class)
                .list();
        EventBasedGateway eventBaseGateway = null;
        if(!eventBaseGateways.isEmpty()){
            eventBaseGateway = eventBaseGateways.get(0);
        }
        IntermediateCatchEvent intermediateCatchEvent = null;
        if (eventBaseGateway != null) {

            List<IntermediateCatchEvent> intermediateCatchEvents = eventBaseGateway.getSucceedingNodes()
                    .filterByType(IntermediateCatchEvent.class).list();

            for (IntermediateCatchEvent ice : intermediateCatchEvents) {
                log.info("IntermediateCatchEvent:{} {}", ice.getId(), ice.getName());

                Collection<EventDefinition> eventDefinitions = ice.getEventDefinitions();

                for (EventDefinition ed : eventDefinitions) {
                    log.info("EventDefinition: {} {}", ed.getId(), ed.getElementType().getTypeName());
                    if ("signalEventDefinition".equals(ed.getElementType().getTypeName())) {
                        intermediateCatchEvent = ice;
                        break;
                    }
                }
                
                if(intermediateCatchEvent != null){
                    break;
                }
            }

        }
        
        if (intermediateCatchEvent != null) {
            event.setExecutionId(intermediateCatchEvent.getId());
        }

        return event;
    }

}
