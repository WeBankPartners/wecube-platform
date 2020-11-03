package com.webank.wecube.platform.workflow.parse;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusMapper;

/**
 * 
 * @author gavin
 *
 */
public abstract class AbstractServiceNodeStartListener extends AbstractServiceNodeListener{
    protected void logServiceNodeStart(DelegateExecution execution) {
        String nodeId = execution.getCurrentActivityId();
        String procInstanceBizKey = execution.getProcessBusinessKey();

        ServiceNodeStatusMapper serviceNodeStatusRepository = SpringApplicationContextUtil
                .getBean(ServiceNodeStatusMapper.class);

        ServiceNodeStatusEntity entity = serviceNodeStatusRepository
                .findOneByProcInstanceBizKeyAndNodeId(procInstanceBizKey, nodeId);

        if (entity == null) {
            getLogger().warn("{} is null for procInstanceBizKey={},nodeId={}", ServiceNodeStatusEntity.class.getSimpleName(),
                    procInstanceBizKey, nodeId);
            throw new IllegalStateException("service node status entity doesnt exist");
        }
        
        Date currTime = new Date();
        entity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        entity.setUpdatedTime(currTime);
        entity.setStatus(TraceStatus.InProgress);
        entity.setStartTime(currTime);
        
        serviceNodeStatusRepository.updateByPrimaryKeySelective(entity);
        
    }
}
