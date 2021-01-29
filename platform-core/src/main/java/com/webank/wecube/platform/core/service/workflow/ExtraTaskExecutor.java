package com.webank.wecube.platform.core.service.workflow;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.workflow.ExtraTaskEntity;
import com.webank.wecube.platform.core.entity.workflow.OperationEventEntity;
import com.webank.wecube.platform.core.repository.workflow.ExtraTaskMapper;
import com.webank.wecube.platform.workflow.WorkflowConstants;

@Service
public class ExtraTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(ExtraTaskExecutor.class);

    @Autowired
    private ExtraTaskMapper extraTaskMapper;

    @Autowired
    private TaskNodeDynamicBindRetryProcessor taskNodeDynamicBindRetryProcessor;

    public void execute() {
        tryProcessNewTasks();
    }

    protected void tryProcessNewTasks() {
        List<ExtraTaskEntity> outstandingTasks = extraTaskMapper.selectAllByStatus(OperationEventEntity.STATUS_NEW);

        if (outstandingTasks == null || outstandingTasks.isEmpty()) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("total {} new tasks to execute.", outstandingTasks.size());
        }

        for (ExtraTaskEntity task : outstandingTasks) {

            tryStart(task);

        }
    }

    private void tryStart(ExtraTaskEntity task) {
        int expectRev = task.getRev();

        task.setStatus(OperationEventEntity.STATUS_PREPROCESS);
        task.setUpdatedTime(new Date());
        task.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        task.setStartTime(new Date());
        task.setRev(expectRev + 1);

        ExtraTaskEntity toUpdateEventEntity = new ExtraTaskEntity();
        toUpdateEventEntity.setId(task.getId());
        toUpdateEventEntity.setUpdatedTime(task.getUpdatedTime());
        toUpdateEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        toUpdateEventEntity.setRev(task.getRev());
        toUpdateEventEntity.setStatus(task.getStatus());
        toUpdateEventEntity.setStartTime(task.getStartTime());

        int updateResult = extraTaskMapper.updateByPrimaryKeySelectiveCas(toUpdateEventEntity, expectRev);
        if (updateResult > 0) {
            log.info("About to process task:{}:{}:{}", task.getId(), task.getTaskSeqNo(), task.getTaskType());
            performStart(task);
        } else {
            log.info("Failed to get lock for extra task:id={},status={}", task.getId(), task.getStatus());
        }
    }

    private void performStart(ExtraTaskEntity task) {
        try {
            if(ExtraTaskEntity.TASK_TYPE_DYNAMIC_BIND_TASK_NODE_RETRY.equalsIgnoreCase(task.getTaskType())){
                taskNodeDynamicBindRetryProcessor.process(task);
            }
        } catch (Exception e) {
            log.error("operation event process starting failed", e);
            tryHandleFailedStart(task, e);
        }
    }

    private void tryHandleFailedStart(ExtraTaskEntity operationEventEntity, Exception e) {
        ExtraTaskEntity toUpdateTask = new ExtraTaskEntity();
        toUpdateTask.setId(operationEventEntity.getId());
        toUpdateTask.setStatus(OperationEventEntity.STATUS_FAILED);
        int expectRev = operationEventEntity.getRev();
        toUpdateTask.setRev(expectRev + 1);
        extraTaskMapper.updateByPrimaryKeySelectiveCas(toUpdateTask, expectRev);
    }

}
