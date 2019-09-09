package com.webank.wecube.core.service.workflow.parse;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.webank.wecube.core.domain.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.core.domain.workflow.entity.TraceStatus;
import com.webank.wecube.core.jpa.workflow.ServiceNodeStatusRepository;
import com.webank.wecube.core.service.workflow.WorkflowConstants;

public abstract class AbstractServiceNodeStartListener extends AbstractServiceNodeListener{
    protected void logServiceNodeStart(DelegateExecution execution) {
        String nodeId = execution.getCurrentActivityId();
        String procInstanceBizKey = execution.getProcessBusinessKey();

        ServiceNodeStatusRepository serviceNodeStatusRepository = SpringApplicationContextUtil
                .getBean(ServiceNodeStatusRepository.class);

        ServiceNodeStatusEntity entity = serviceNodeStatusRepository
                .findOneByProcInstanceBizKeyAndNodeId(procInstanceBizKey, nodeId);

        if (entity == null) {
            getLogger().error("{} is null for procInstanceBizKey={},nodeId={}", ServiceNodeStatusEntity.class.getSimpleName(),
                    procInstanceBizKey, nodeId);
            throw new IllegalStateException("service node status entity doesnt exist");
        }
        
        Date currTime = new Date();
        entity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        entity.setUpdatedTime(currTime);
        entity.setStatus(TraceStatus.InProgress);
        entity.setStartTime(currTime);
        
        serviceNodeStatusRepository.save(entity);
        
    }
}
