package com.webank.wecube.platform.workflow.parse;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SubProcessEndListener  extends AbstractServiceNodeEndListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(SubProcessEndListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {


        log.info("-----------------------------------------------------------------");
        log.info("SubProcess END:  {},  {}, {}",
                execution.getCurrentActivityName(), execution.getCurrentActivityId(),  
                execution.getProcessBusinessKey());
        log.info("-----------------------------------------------------------------");
        
        logServiceNodeEnd(execution);

    }

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    
}
