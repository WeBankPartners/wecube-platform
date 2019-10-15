package com.webank.wecube.platform.workflow.parse;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.workflow.WorkflowConstants;

@Component
public class EndEventListener implements ExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(EndEventListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        log.info("EndEvent ending, instance {} businessKey {} execution {} activityId {}",
                execution.getProcessInstanceId(), execution.getBusinessKey(), execution.getId(),
                execution.getCurrentActivityId());

        if (execution instanceof ExecutionEntity) {
            ExecutionEntity entity = (ExecutionEntity) execution;
            Object typeProperty = entity.getActivity().getProperty("type");
            if (typeProperty != null && (typeProperty instanceof String)) {
                if ("errorEndEvent".equalsIgnoreCase((String) typeProperty)) {
                    log.warn("process {} ,businessKey {} going with error", execution.getProcessInstanceId(),
                            execution.getBusinessKey());

                    execution.setVariable(WorkflowConstants.VAR_KEY_PROCESS_WITH_ERROR, true);

                }
            }
        }

    }

}
