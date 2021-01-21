package com.webank.wecube.platform.core.service.event;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.workflow.OperationEventEntity;
import com.webank.wecube.platform.core.repository.workflow.OperationEventMapper;
import com.webank.wecube.platform.workflow.WorkflowConstants;

@Service
public class OperationEventsExecutor {

    private static final Logger log = LoggerFactory.getLogger(OperationEventsExecutor.class);

    @Autowired
    private OperationEventMapper operationEventRepository;
    @Autowired
    private OperationEventProcStarter operationEventProcStarter;

    public void execute() {
        // process new
        tryProcessNewOperationEvents();

        // process Faulted
        tryProcessFailedOperationEvents();

        // process timeouted preprocess
        tryProcessPreprocessOperationEvents();
    }

    protected void tryProcessFailedOperationEvents() {
        List<OperationEventEntity> outstandingOperationEventEntities = operationEventRepository
                .selectAllByStatus(OperationEventEntity.STATUS_FAILED);

        if (outstandingOperationEventEntities == null || outstandingOperationEventEntities.isEmpty()) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("total {} failed operation events to execute.", outstandingOperationEventEntities.size());
        }

        for (OperationEventEntity operationEventEntity : outstandingOperationEventEntities) {
            if (!needRetryFailedEvent(operationEventEntity)) {
                continue;
            }

            int expectRev = operationEventEntity.getRev() == null ? 0 : operationEventEntity.getRev();

            operationEventEntity.setStatus(OperationEventEntity.STATUS_PREPROCESS);
            operationEventEntity.setUpdatedTime(new Date());
            operationEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            operationEventEntity.setStartTime(new Date());
            operationEventEntity.setRev(expectRev + 1);
            int priority = (operationEventEntity.getPriority() == null ? 0 : operationEventEntity.getPriority());
            operationEventEntity.setPriority(priority - 1);

            OperationEventEntity toUpdateEventEntity = new OperationEventEntity();
            toUpdateEventEntity.setId(operationEventEntity.getId());
            toUpdateEventEntity.setUpdatedTime(operationEventEntity.getUpdatedTime());
            toUpdateEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            toUpdateEventEntity.setRev(operationEventEntity.getRev());
            toUpdateEventEntity.setStatus(operationEventEntity.getStatus());
            toUpdateEventEntity.setStartTime(operationEventEntity.getStartTime());
            toUpdateEventEntity.setPriority(operationEventEntity.getPriority());

            int updateResult = operationEventRepository.updateByPrimaryKeySelectiveCas(toUpdateEventEntity, expectRev);
            if (updateResult > 0) {
                log.info("About to process operation event:{}:{}:{}", operationEventEntity.getId(),
                        operationEventEntity.getEventSeqNo(), operationEventEntity.getEventType());
                try {
                    operationEventProcStarter.startOperationEventProcess(operationEventEntity);
                } catch (Exception e) {
                    log.error("operation event process starting failed", e);
                    operationEventEntity.setStatus(OperationEventEntity.STATUS_FAILED);
                    expectRev = operationEventEntity.getRev();
                    operationEventEntity.setRev(expectRev + 1);
                    operationEventRepository.updateByPrimaryKeySelectiveCas(operationEventEntity, expectRev);
                }
            } else {
                log.info("Failed to get lock for operation event:id={},status={}", operationEventEntity.getId(),
                        operationEventEntity.getStatus());
            }
        }
    }

    private boolean needRetryFailedEvent(OperationEventEntity e) {
        if (e.getPriority() == null) {
            return true;
        }

        if (e.getPriority() <= -5) {
            return false;
        }

        return true;
    }

    protected void tryProcessPreprocessOperationEvents() {
        List<OperationEventEntity> outstandingOperationEventEntities = operationEventRepository
                .selectAllByStatus(OperationEventEntity.STATUS_PREPROCESS);

        if (outstandingOperationEventEntities == null || outstandingOperationEventEntities.isEmpty()) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("total {} preprocess operation events to execute.", outstandingOperationEventEntities.size());
        }

        for (OperationEventEntity operationEventEntity : outstandingOperationEventEntities) {
            if (!needTryStartPreprocessEventOnceMore(operationEventEntity)) {
                continue;
            }

            int expectRev = operationEventEntity.getRev();

            operationEventEntity.setStatus(OperationEventEntity.STATUS_PREPROCESS);
            operationEventEntity.setUpdatedTime(new Date());
            operationEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            operationEventEntity.setStartTime(new Date());
            operationEventEntity.setRev(expectRev + 1);
            int priority = (operationEventEntity.getPriority() == null ? 0 : operationEventEntity.getPriority());
            operationEventEntity.setPriority(priority - 1);

            OperationEventEntity toUpdateEventEntity = new OperationEventEntity();
            toUpdateEventEntity.setId(operationEventEntity.getId());
            toUpdateEventEntity.setUpdatedTime(operationEventEntity.getUpdatedTime());
            toUpdateEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            toUpdateEventEntity.setRev(operationEventEntity.getRev());
            toUpdateEventEntity.setStatus(operationEventEntity.getStatus());
            toUpdateEventEntity.setStartTime(operationEventEntity.getStartTime());
            toUpdateEventEntity.setPriority(operationEventEntity.getPriority());

            int updateResult = operationEventRepository.updateByPrimaryKeySelectiveCas(toUpdateEventEntity, expectRev);
            if (updateResult > 0) {
                log.info("About to process operation event:{}:{}:{}", operationEventEntity.getId(),
                        operationEventEntity.getEventSeqNo(), operationEventEntity.getEventType());
                try {
                    operationEventProcStarter.startOperationEventProcess(operationEventEntity);
                } catch (Exception e) {
                    log.error("operation event process starting failed", e);
                    operationEventEntity.setStatus(OperationEventEntity.STATUS_FAILED);
                    expectRev = operationEventEntity.getRev();
                    operationEventEntity.setRev(expectRev + 1);
                    operationEventRepository.updateByPrimaryKeySelectiveCas(operationEventEntity, expectRev);
                }
            } else {
                log.info("Failed to get lock for operation event:id={},status={}", operationEventEntity.getId(),
                        operationEventEntity.getStatus());
            }
        }
    }

    private boolean needTryStartPreprocessEventOnceMore(OperationEventEntity operationEventEntity) {
        if (operationEventEntity.getStartTime() == null) {
            return false;
        }

        Date baselineDate = DateUtils.addMinutes(operationEventEntity.getStartTime(), 10);
        if (baselineDate.before(new Date())) {
            return true;
        }

        return false;

    }

    protected void tryProcessNewOperationEvents() {
        List<OperationEventEntity> outstandingOperationEventEntities = operationEventRepository
                .selectAllByStatus(OperationEventEntity.STATUS_NEW);

        if (outstandingOperationEventEntities == null || outstandingOperationEventEntities.isEmpty()) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("total {} new operation events to execute.", outstandingOperationEventEntities.size());
        }

        for (OperationEventEntity operationEventEntity : outstandingOperationEventEntities) {

            int expectRev = operationEventEntity.getRev();

            operationEventEntity.setStatus(OperationEventEntity.STATUS_PREPROCESS);
            operationEventEntity.setUpdatedTime(new Date());
            operationEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            operationEventEntity.setStartTime(new Date());
            operationEventEntity.setRev(expectRev + 1);

            OperationEventEntity toUpdateEventEntity = new OperationEventEntity();
            toUpdateEventEntity.setId(operationEventEntity.getId());
            toUpdateEventEntity.setUpdatedTime(operationEventEntity.getUpdatedTime());
            toUpdateEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            toUpdateEventEntity.setRev(operationEventEntity.getRev());
            toUpdateEventEntity.setStatus(operationEventEntity.getStatus());
            toUpdateEventEntity.setStartTime(operationEventEntity.getStartTime());

            int updateResult = operationEventRepository.updateByPrimaryKeySelectiveCas(toUpdateEventEntity, expectRev);
            if (updateResult > 0) {
                log.info("About to process operation event:{}:{}:{}", operationEventEntity.getId(),
                        operationEventEntity.getEventSeqNo(), operationEventEntity.getEventType());
                try {
                    operationEventProcStarter.startOperationEventProcess(operationEventEntity);
                } catch (Exception e) {
                    log.error("operation event process starting failed", e);
                    operationEventEntity.setStatus(OperationEventEntity.STATUS_FAILED);
                    expectRev = operationEventEntity.getRev();
                    operationEventEntity.setRev(expectRev + 1);
                    operationEventRepository.updateByPrimaryKeySelectiveCas(operationEventEntity, expectRev);
                }
            } else {
                log.info("Failed to get lock for operation event:id={},status={}", operationEventEntity.getId(),
                        operationEventEntity.getStatus());
            }

        }
    }
}
