package com.webank.wecube.core.service.workflow.delegate;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;

import com.webank.wecube.core.domain.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.core.domain.workflow.entity.TraceStatus;
import com.webank.wecube.core.jpa.workflow.ServiceNodeStatusRepository;
import com.webank.wecube.core.service.workflow.WorkflowConstants;
import com.webank.wecube.core.service.workflow.parse.SpringApplicationContextUtil;

public abstract class AbstractServiceExceptionHandleDelegate {
    protected void logServiceNodeException(DelegateExecution execution, TraceStatus traceStatus, String idPrefix){
        String activityId = execution.getCurrentActivityId();
        if(activityId == null){
            return;
        }
        
        if(!activityId.startsWith(idPrefix)){
            return;
        }
        
        String nodeId = activityId.substring(idPrefix.length());
        String procInstanceBizKey = execution.getProcessBusinessKey();
        
        ServiceNodeStatusRepository repository = SpringApplicationContextUtil.getBean(ServiceNodeStatusRepository.class);
        
        ServiceNodeStatusEntity entity = repository.findOneByProcInstanceBizKeyAndNodeId(procInstanceBizKey, nodeId);
        
        if(entity == null){
            getLogger().error("{} doesnt exist for procInstanceBizKey={},nodeId={}", procInstanceBizKey, nodeId);
            throw new IllegalStateException("entity doesnt exist");
        }
        
        entity.setUpdatedBy(WorkflowConstants.DEFAULT_USER);
        entity.setUpdatedTime(new Date());
        entity.setStatus(traceStatus);
        
        repository.save(entity);
    }
    
    protected abstract Logger getLogger();
}
