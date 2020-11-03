package com.webank.wecube.platform.workflow.delegate;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;

import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.parse.SpringApplicationContextUtil;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusMapper;

/**
 * 
 * @author gavin
 *
 */
public abstract class AbstractServiceExceptionHandleDelegate {
    protected void logServiceNodeException(DelegateExecution execution, TraceStatus traceStatus, String idPrefix) {
        String activityId = execution.getCurrentActivityId();
        if (activityId == null) {
            return;
        }

        if (!activityId.startsWith(idPrefix)) {
            return;
        }

        String nodeId = activityId.substring(idPrefix.length());
        String procInstanceBizKey = execution.getProcessBusinessKey();

        ServiceNodeStatusMapper repository = SpringApplicationContextUtil
                .getBean(ServiceNodeStatusMapper.class);

        ServiceNodeStatusEntity entity = repository.findOneByProcInstanceBizKeyAndNodeId(procInstanceBizKey, nodeId);

        if (entity == null) {
            getLogger().warn("{} doesnt exist for procInstanceBizKey={},nodeId={}",
                    ServiceNodeStatusEntity.class.getSimpleName(), procInstanceBizKey, nodeId);
            throw new IllegalStateException("Entity doesnt exist");
        }

        entity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        entity.setUpdatedTime(new Date());
        entity.setStatus(traceStatus);

        repository.updateByPrimaryKeySelective(entity);
    }

    protected abstract Logger getLogger();
}
