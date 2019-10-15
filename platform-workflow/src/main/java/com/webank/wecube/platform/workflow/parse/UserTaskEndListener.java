package com.webank.wecube.platform.workflow.parse;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusRepository;

@Component
public class UserTaskEndListener implements ExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(UserTaskEndListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (log.isDebugEnabled()) {
            log.info("UserTask END:  {},  {}, {}, {}", execution.getCurrentActivityName(),
                    execution.getCurrentActivityId(), execution.getProcessBusinessKey(),
                    execution.getActivityInstanceId());
        }

        logUserTaskEnd(execution);
    }

    protected void logUserTaskEnd(DelegateExecution execution) {
        ServiceNodeStatusRepository respository = SpringApplicationContextUtil
                .getBean(ServiceNodeStatusRepository.class);
        Date currTime = new Date();
        ServiceNodeStatusEntity entity = respository.findOneByProcInstanceBizKeyAndNodeIdAndStatus(
                execution.getProcessBusinessKey(), execution.getCurrentActivityId(), TraceStatus.InProgress);

        if (entity != null) {
            entity.setUpdatedBy("system");
            entity.setUpdatedTime(currTime);
            entity.setEndTime(currTime);
            entity.setStatus(TraceStatus.Completed);

            respository.save(entity);
        } else {
            log.warn("cannot find user task status entity for processInstBizKey={},nodeId={}",
                    execution.getProcessBusinessKey(), execution.getCurrentActivityId());
        }
    }

}
