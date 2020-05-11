package com.webank.wecube.platform.workflow.parse;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("ServiceTaskStartListener")
public class ServiceTaskStartListener extends AbstractServiceNodeStartListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(ServiceTaskStartListener.class);
    
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        log.info("ServiceTask START:  {},  {}, {}", execution.getCurrentActivityName(), execution.getCurrentActivityId(),
                execution.getProcessBusinessKey());

        if(isCustomServiceTask(execution)){
            return;
        }
        
        logServiceNodeStart(execution);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
