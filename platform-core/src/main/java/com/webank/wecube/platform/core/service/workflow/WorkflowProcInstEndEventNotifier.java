package com.webank.wecube.platform.core.service.workflow;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.client.http.JwtSsoRestTemplate;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.dto.event.OperationEventNotificationDto;
import com.webank.wecube.platform.core.entity.workflow.OperationEventEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.WorkflowNotifyEvent;
import com.webank.wecube.platform.core.repository.workflow.OperationEventMapper;

@Service
public class WorkflowProcInstEndEventNotifier {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProcInstEndEventNotifier.class);

    @Autowired
    private OperationEventMapper operationEventRepository;

    @Autowired
    @Qualifier("jwtSsoRestTemplate")
    private JwtSsoRestTemplate restTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    public void notify(WorkflowNotifyEvent event, PluginInvocationCommand cmd, ProcInstInfoEntity procInstEntity) {
        List<OperationEventEntity> operationEventEntities = operationEventRepository
                .findAllByProcInstKey(procInstEntity.getProcInstKey());

        if (operationEventEntities == null || operationEventEntities.isEmpty()) {
            log.debug("none operation event to notify");
            return;
        }

        for (OperationEventEntity operationEventEntity : operationEventEntities) {
            if (OperationEventEntity.STATUS_COMPLETED.equalsIgnoreCase(operationEventEntity.getStatus())) {
                continue;
            }
            if (operationEventEntity.getIsNotifyRequired() != null && operationEventEntity.getIsNotifyRequired() == true) {

                String url = String.format("http://%s/%s", applicationProperties.getGatewayUrl(),
                        operationEventEntity.getNotifyEndpoint());

                // notify
                OperationEventNotificationDto notificationDto = new OperationEventNotificationDto();
                notificationDto.setEventSeqNo(operationEventEntity.getEventSeqNo());
                notificationDto.setEventType(operationEventEntity.getEventType());
                notificationDto.setSourceSubSystem(operationEventEntity.getSrcSubSystem());
                notificationDto.setStatus(OperationEventEntity.STATUS_COMPLETED);

                try {
                    String resp = restTemplate.postForObject(url, notificationDto, String.class);
                    log.info("notify response:{}", resp);
                } catch (Exception e) {
                    log.error("notification failed", e);
                }

                operationEventEntity.setIsNotified(true);
            }

            operationEventEntity.setStatus(OperationEventEntity.STATUS_COMPLETED);
            operationEventEntity.setEndTime(new Date());
            operationEventEntity.setUpdatedTime(new Date());

            operationEventRepository.updateByPrimaryKeySelective(operationEventEntity);
        }
    }
}
