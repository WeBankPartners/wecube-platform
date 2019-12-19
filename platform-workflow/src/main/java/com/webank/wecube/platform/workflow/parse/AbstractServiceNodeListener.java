package com.webank.wecube.platform.workflow.parse;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;

/**
 * 
 * @author gavin
 *
 */
public abstract class AbstractServiceNodeListener {
    protected boolean isCustomServiceTask(DelegateExecution execution) {

        String activityId = execution.getCurrentActivityId();
        if (activityId != null) {
            if (activityId.startsWith("srvBeanST-") || activityId.startsWith("srvTimeOutBeanST-")
                    || activityId.startsWith("srvFailBeanST-")) {
                return true;
            }
        }
        return false;
    }
    
    protected abstract Logger getLogger();
}
