package com.webank.wecube.core.service.workflow.delegate;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.core.domain.workflow.ServiceInvocationEvent;
import com.webank.wecube.core.domain.workflow.ServiceInvocationEventImpl;
import com.webank.wecube.core.domain.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.core.domain.workflow.entity.TraceStatus;
import com.webank.wecube.core.jpa.workflow.ServiceNodeStatusRepository;
import com.webank.wecube.core.service.workflow.WorkflowConstants;
import com.webank.wecube.core.service.workflow.parse.SpringApplicationContextUtil;

@Component("taskDispatcher")
public class PlugableApplicationTaskDispatcher implements JavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(PlugableApplicationTaskDispatcher.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String processInstanceBizKey = execution.getBusinessKey();
        String processInstanceId = execution.getProcessInstanceId();
        String processDefinitionId = execution.getProcessDefinitionId();
        log.info("start to call plugin with service code [{}] and bizKey [{}]", processInstanceId,
                processInstanceBizKey);

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

        event.setExecutionId(execution.getId());
        event.setBusinessKey(execution.getProcessBusinessKey());
        event.setInstanceId(execution.getProcessInstanceId());

        event.setEventSourceId(execution.getCurrentActivityId());
        event.setEventSourceName(execution.getCurrentActivityName());
        
        event.setEventType(ServiceInvocationEvent.EventType.SERVICE_INVOCATION);

        return event;
    }

}
