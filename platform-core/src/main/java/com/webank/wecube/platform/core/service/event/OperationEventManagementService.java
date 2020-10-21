package com.webank.wecube.platform.core.service.event;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.event.OperationEventDto;
import com.webank.wecube.platform.core.dto.event.OperationEventResultDto;
import com.webank.wecube.platform.core.entity.event.OperationEventEntity;
import com.webank.wecube.platform.core.jpa.event.OperationEventRepository;

@Service
public class OperationEventManagementService {
    
    public static final String OPER_MODE_INSTANT = "instant";
    public static final String OPER_MODE_DEFER = "defer";

    private static final Logger log = LoggerFactory.getLogger(OperationEventManagementService.class);
    
    public static final String BOOLEAN_TRUE = "Y";
    public static final String BOOLEAN_FALSE = "N";

    @Autowired
    private OperationEventRepository operationEventRepository;

    //TODO
    public OperationEventResultDto reportOperationEvent(OperationEventDto eventDto) {
        validateOperationEventInput(eventDto);
        
        String eventSeqNo = eventDto.getEventSeqNo();

        List<OperationEventEntity> operationEventEntities = operationEventRepository.findAllByEventSeqNo(eventSeqNo);
        
        if(operationEventEntities != null && !operationEventEntities.isEmpty()){
            log.error("operation event already exists,eventSeqNo={}", eventSeqNo);
            throw new WecubeCoreException("3006","Operation event already exists.");
        }
        
        OperationEventEntity newOperationEventEntity = new OperationEventEntity();
        newOperationEventEntity.setEventSeqNo(eventDto.getEventSeqNo());
        newOperationEventEntity.setEventType(eventDto.getEventType());
        newOperationEventEntity.setNotifyRequired(parseBoolean(eventDto.getNotifyRequired()));
        newOperationEventEntity.setNotifyEndpoint(eventDto.getNotifyEndpoint());
        newOperationEventEntity.setOperationData(eventDto.getOperationData());
        newOperationEventEntity.setOperationKey(eventDto.getOperationKey());
        newOperationEventEntity.setOperationUser(eventDto.getOperationUser());
        newOperationEventEntity.setSourceSubSystem(eventDto.getSourceSubSystem());
        if(OPER_MODE_INSTANT.equalsIgnoreCase(eventDto.getOperationMode())){
            newOperationEventEntity.setStatus(OperationEventEntity.STATUS_IN_PROGRESS);
        }else{
            newOperationEventEntity.setStatus(OperationEventEntity.STATUS_NEW);
        }

        OperationEventEntity savedOperEventEntity = operationEventRepository.saveAndFlush(newOperationEventEntity);
        
        if(OPER_MODE_INSTANT.equalsIgnoreCase(eventDto.getOperationMode())){
            //TODO
        }
        
        //TODO
        OperationEventResultDto result = new OperationEventResultDto();
        return result;
    }
    
    private Boolean parseBoolean(String booleanValueAsString){
        if(StringUtils.isBlank(booleanValueAsString)){
            return null;
        }
        
        if(BOOLEAN_TRUE.equalsIgnoreCase(booleanValueAsString)){
            return true;
        }
        
        if(BOOLEAN_FALSE.equalsIgnoreCase(booleanValueAsString)){
            return false;
        }
        
        throw new WecubeCoreException("3007","Boolean value must be 'Y' or 'N'");
    }

    private void validateOperationEventInput(OperationEventDto eventDto) {
        if (eventDto == null) {
            throw new WecubeCoreException("3008","Illegal input,cannot be null.");
        }

        if (StringUtils.isBlank(eventDto.getEventSeqNo()) || StringUtils.isBlank(eventDto.getEventType())
                || StringUtils.isBlank(eventDto.getSourceSubSystem())
                || StringUtils.isBlank(eventDto.getOperationKey())) {

            log.error("mandatory fields must provide,eventSeqNo={},eventType={},sourceSubSystem={},operationKey={}",
                    eventDto.getEventSeqNo(), eventDto.getEventType(), eventDto.getSourceSubSystem(),
                    eventDto.getOperationKey());
            throw new WecubeCoreException("3009","Mandatory fields must provide.");
        }
    }
}
