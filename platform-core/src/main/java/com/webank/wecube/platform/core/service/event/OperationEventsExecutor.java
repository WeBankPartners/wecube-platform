package com.webank.wecube.platform.core.service.event;

import java.util.Date;
import java.util.List;

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
        
        tryProcessNewOperationEvents();

        // TODO to consider faulted status and retry
        // process NEW
        

        // process Faulted

        // process InProgress
    }
    
    protected void tryProcessPreprocessOperationEvents(){
        
    }
    
    protected void tryProcessFailedOperationEvents(){
        
    }
    
    protected void tryProcessNewOperationEvents(){
        List<OperationEventEntity> outstandingOperationEventEntities = operationEventRepository
                .selectAllByStatus(OperationEventEntity.STATUS_NEW);

        if (outstandingOperationEventEntities == null) {
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("total {} new operation events to execute.", outstandingOperationEventEntities.size());
        }

        for (OperationEventEntity operationEventEntity : outstandingOperationEventEntities) {

            OperationEventEntity toUpdateEventEntity = new OperationEventEntity();

            int expectRev = operationEventEntity.getRev();

            operationEventEntity.setStatus(OperationEventEntity.STATUS_IN_PROGRESS);
            operationEventEntity.setUpdatedTime(new Date());
            operationEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            operationEventEntity.setStartTime(new Date());
            operationEventEntity.setRev(expectRev + 1);

            toUpdateEventEntity.setId(operationEventEntity.getId());
            toUpdateEventEntity.setUpdatedTime(operationEventEntity.getUpdatedTime());
            toUpdateEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            toUpdateEventEntity.setRev(operationEventEntity.getRev());
            toUpdateEventEntity.setStatus(OperationEventEntity.STATUS_PREPROCESS);
            toUpdateEventEntity.setStartTime(operationEventEntity.getStartTime());

            int updateResult = operationEventRepository.updateByPrimaryKeySelectiveCas(toUpdateEventEntity, expectRev);
            if (updateResult > 0) {
                log.info("About to process operation event:{}:{}:{}", operationEventEntity.getId(),
                        operationEventEntity.getEventSeqNo(), operationEventEntity.getEventType());
                try {
                    operationEventProcStarter.startOperationEventProcess(operationEventEntity);
                } catch (Exception e) {
                    // TODO
                    log.error("operation event process starting failed", e);
                    operationEventEntity.setStatus(OperationEventEntity.STATUS_FAILED);
                    operationEventRepository.updateByPrimaryKeySelective(operationEventEntity);
                }
            } else {
                log.info("Failed to get lock for operation event:id={},status={}", operationEventEntity.getId(),
                        operationEventEntity.getStatus());
            }

        }
    }
}
