package com.webank.wecube.platform.workflow.parse;

import java.util.Arrays;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.workflow.WorkflowConstants;

/**
 * 
 * @author gavin
 *
 */
@Component("SubProcessStartListener")
public class SubProcessStartListener extends AbstractServiceNodeStartListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(SubProcessStartListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        if (log.isDebugEnabled()) {

            log.info("SubProcess START:  {},  {}, {}", execution.getCurrentActivityName(),
                    execution.getCurrentActivityId(), execution.getProcessBusinessKey());
        }

        log.info("remove variables:{} {} {}", WorkflowConstants.VAR_KEY_SUBPROCESS_WITH_ERROR,
                 WorkflowConstants.VAR_KEY_USER_ACT);

        execution.removeVariables(Arrays.asList(WorkflowConstants.VAR_KEY_SUBPROCESS_WITH_ERROR,
                 WorkflowConstants.VAR_KEY_USER_ACT));

        logServiceNodeStart(execution);

    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
