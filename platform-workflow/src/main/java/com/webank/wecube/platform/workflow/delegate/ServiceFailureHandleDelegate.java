package com.webank.wecube.platform.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.model.TraceStatus;

/**
 * 
 * @author gavin
 *
 */
@Service("srvFailBean")
public class ServiceFailureHandleDelegate extends AbstractServiceExceptionHandleDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ServiceFailureHandleDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        logServiceNodeException(execution, TraceStatus.Faulted, WorkflowConstants.PREFIX_SRV_BEAN_FAILURE);
    }
    
    protected Logger getLogger() {
        return log;
    }

}
