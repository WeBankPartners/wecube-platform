package com.webank.wecube.platform.core.service.event;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.entity.workflow.OperationEventEntity;
import com.webank.wecube.platform.core.repository.workflow.OperationEventMapper;

@Service
public class OperationEventsExecutor {

    private static final Logger log = LoggerFactory.getLogger(OperationEventsExecutor.class);

    @Autowired
    private OperationEventMapper operationEventRepository;
    @Autowired
    private OperationEventProcStarter operationEventProcStarter;

    public void execute() {
        List<OperationEventEntity> outstandingOperationEventEntities = operationEventRepository
                .selectAllByStatus(OperationEventEntity.STATUS_NEW);
        
        if (outstandingOperationEventEntities == null ) {
            return;
        }
        
        if (log.isInfoEnabled()) {
            log.info("total {} OperationEventEntity to execute.", outstandingOperationEventEntities.size());
        }

        for (OperationEventEntity operationEventEntity : outstandingOperationEventEntities) {
            try {
                operationEventEntity.setStatus(OperationEventEntity.STATUS_IN_PROGRESS);
                operationEventEntity.setUpdatedTime(new Date());
                operationEventEntity.setStartTime(new Date());
                
                operationEventRepository.updateByPrimaryKeySelective(operationEventEntity);
                operationEventProcStarter.startOperationEventProcess(operationEventEntity);
            } catch (Exception e) {
                log.error("operation event process starting failed", e);
            }
        }
    }
}
