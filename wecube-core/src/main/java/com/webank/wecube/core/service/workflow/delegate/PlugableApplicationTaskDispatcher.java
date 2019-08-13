package com.webank.wecube.core.service.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.core.domain.workflow.ServiceInvocationEvent;
import com.webank.wecube.core.domain.workflow.ServiceInvocationEventImpl;

@Component("taskDispatcher")
public class PlugableApplicationTaskDispatcher implements JavaDelegate {
    private static final Logger log = LoggerFactory.getLogger(PlugableApplicationTaskDispatcher.class);
    public static final String W3_NS_URI = "http://www.webank.com/schema/we3/1.0";
    public static final String ATTR_NAME_SERVICE_CODE = "serviceCode";

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
