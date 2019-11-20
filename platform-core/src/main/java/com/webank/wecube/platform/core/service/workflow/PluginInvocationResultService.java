package com.webank.wecube.platform.core.service.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEvent.EventType;
import com.webank.wecube.platform.workflow.model.ServiceInvocationEventImpl;

/**
 * 
 * @author gavin
 *
 */
@Service
public class PluginInvocationResultService {
    private static final Logger log = LoggerFactory.getLogger(PluginInvocationResultService.class);

    @Autowired
    private WorkflowEngineService workflowEngineService;

    public void responsePluginInterfaceInvocation(PluginInvocationResult result) {
        if (log.isInfoEnabled()) {
            log.info("response plugin interface invocation:{}", result);
        }
        
        if(result == null){
            throw new IllegalArgumentException();
        }

        ServiceInvocationEventImpl event = new ServiceInvocationEventImpl();
        event.setBusinessKey(result.getProcInstKey());
        event.setDefinitionId(result.getProcDefId());
        event.setDefinitionKey(result.getProcDefKey());
        event.setDefinitionVersion(result.getProcDefVersion());
        event.setEventId(String.valueOf(System.currentTimeMillis()));
        event.setEventType(EventType.SERVICE_INVOCATION_RESULT);
        event.setInstanceId(result.getProcInstId());
        event.setResult(result.getResultCode());
        event.setExecutionId(result.getExecutionId());

        workflowEngineService.handleServiceInvocationResult(event);
    }

}
