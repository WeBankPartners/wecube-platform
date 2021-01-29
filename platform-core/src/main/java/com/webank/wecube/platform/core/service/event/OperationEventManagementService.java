package com.webank.wecube.platform.core.service.event;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.event.OperationEventDto;
import com.webank.wecube.platform.core.dto.event.OperationEventResultDto;
import com.webank.wecube.platform.core.entity.workflow.OperationEventEntity;
import com.webank.wecube.platform.core.repository.workflow.OperationEventMapper;
import com.webank.wecube.platform.workflow.WorkflowConstants;

/**
 * 
 * @author gavin
 *
 */
@Service
public class OperationEventManagementService {
    private static final Logger log = LoggerFactory.getLogger(OperationEventManagementService.class);

    public static final String BOOLEAN_TRUE = "Y";
    public static final String BOOLEAN_FALSE = "N";

    @Autowired
    private OperationEventMapper operationEventMapper;

    @Autowired
    private OperationEventProcStarter operationEventProcStarter;

    /**
     * 
     * @param eventDto
     * @return
     */
    public OperationEventResultDto reportOperationEvent(OperationEventDto eventDto) {
        validateOperationEventInput(eventDto);

        String eventSeqNo = eventDto.getEventSeqNo();

        List<OperationEventEntity> operationEventEntities = operationEventMapper.selectAllByEventSeqNo(eventSeqNo);

        if (operationEventEntities != null && !operationEventEntities.isEmpty()) {
            log.error("operation event already exists,eventSeqNo={}", eventSeqNo);
            throw new WecubeCoreException("3006", "Operation event already exists.");
        }

        OperationEventEntity newOperationEventEntity = new OperationEventEntity();
        newOperationEventEntity.setEventSeqNo(eventDto.getEventSeqNo());
        newOperationEventEntity.setEventType(eventDto.getEventType());
        newOperationEventEntity.setIsNotifyRequired(parseBoolean(eventDto.getNotifyRequired()));
        newOperationEventEntity.setNotifyEndpoint(eventDto.getNotifyEndpoint());
        newOperationEventEntity.setOperData(eventDto.getOperationData());
        newOperationEventEntity.setOperKey(eventDto.getOperationKey());
        newOperationEventEntity.setOperUser(eventDto.getOperationUser());
        newOperationEventEntity.setSrcSubSystem(eventDto.getSourceSubSystem());
        newOperationEventEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        newOperationEventEntity.setCreatedTime(new Date());
        newOperationEventEntity.setRev(OperationEventEntity.REV_INIT);
        newOperationEventEntity.setPriority(OperationEventEntity.PRIORITY_INIT);
        if (OperationEventEntity.OPER_MODE_INSTANT.equalsIgnoreCase(eventDto.getOperationMode())) {
            newOperationEventEntity.setStatus(OperationEventEntity.STATUS_IN_PROGRESS);
            newOperationEventEntity.setOperMode(OperationEventEntity.OPER_MODE_INSTANT);
        } else {
            newOperationEventEntity.setStatus(OperationEventEntity.STATUS_NEW);
            newOperationEventEntity.setOperMode(OperationEventEntity.OPER_MODE_DEFER);
        }

        operationEventMapper.insert(newOperationEventEntity);

        if (OperationEventEntity.OPER_MODE_INSTANT.equalsIgnoreCase(eventDto.getOperationMode())) {
            return handleInstantOperationEvent(newOperationEventEntity);
        }

        return fromOperationEventEntity(newOperationEventEntity);
    }

    private OperationEventResultDto handleInstantOperationEvent(OperationEventEntity instantOperEventEntity) {
        try {
            OperationEventEntity entity = operationEventProcStarter
                    .startInstantOperationEventProcess(instantOperEventEntity);
            return fromOperationEventEntity(entity);
        } catch (Exception e) {
            log.error("failed to process instant operation event", e);
            instantOperEventEntity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
            instantOperEventEntity.setUpdatedTime(new Date());
            instantOperEventEntity.setStatus(OperationEventEntity.STATUS_FAULTED);
            instantOperEventEntity.setPriority(-100);
            throw new WecubeCoreException("Failed to process instant operation event");
        }

    }

    private OperationEventResultDto fromOperationEventEntity(OperationEventEntity entity) {
        OperationEventResultDto result = new OperationEventResultDto();
        result.setEventSeqNo(entity.getEventSeqNo());
        result.setEventType(entity.getEventType());
        result.setNotifyEndpoint(entity.getNotifyEndpoint());
        result.setNotifyRequired(String.valueOf(entity.getIsNotifyRequired()));
        result.setOperationData(entity.getOperData());
        result.setOperationKey(entity.getOperKey());
        result.setOperationUser(entity.getOperUser());
        result.setProcDefId(entity.getProcDefId());
        result.setProcInstId(entity.getProcInstId());
        result.setProcInstKey(entity.getProcInstKey());
        result.setSourceSubSystem(entity.getSrcSubSystem());
        result.setStatus(entity.getStatus());

        return result;
    }

    private Boolean parseBoolean(String booleanValueAsString) {
        if (StringUtils.isBlank(booleanValueAsString)) {
            return null;
        }

        if (BOOLEAN_TRUE.equalsIgnoreCase(booleanValueAsString)) {
            return true;
        }

        if (BOOLEAN_FALSE.equalsIgnoreCase(booleanValueAsString)) {
            return false;
        }

        throw new WecubeCoreException("3007", "Boolean value must be 'Y' or 'N'");
    }

    private void validateOperationEventInput(OperationEventDto eventDto) {
        if (eventDto == null) {
            throw new WecubeCoreException("3008", "Illegal input,cannot be null.");
        }

        if (StringUtils.isBlank(eventDto.getEventSeqNo()) || StringUtils.isBlank(eventDto.getEventType())
                || StringUtils.isBlank(eventDto.getSourceSubSystem())
                || StringUtils.isBlank(eventDto.getOperationKey())) {

            log.error("mandatory fields must provide,eventSeqNo={},eventType={},sourceSubSystem={},operationKey={}",
                    eventDto.getEventSeqNo(), eventDto.getEventType(), eventDto.getSourceSubSystem(),
                    eventDto.getOperationKey());
            throw new WecubeCoreException("3009", "Mandatory fields must provide.");
        }
    }
}
