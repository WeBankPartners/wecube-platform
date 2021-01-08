package com.webank.wecube.platform.workflow.parse;

import java.util.Date;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.workflow.WorkflowConstants;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.NodeType;
import com.webank.wecube.platform.workflow.model.TraceStatus;
import com.webank.wecube.platform.workflow.repository.ServiceNodeStatusMapper;

/**
 * 
 * @author gavin
 *
 */
@Component
public class UserTaskStartListener implements ExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(UserTaskStartListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("UserTask START:  {},  {}, {}, {}", execution.getCurrentActivityName(),
                    execution.getCurrentActivityId(), execution.getProcessBusinessKey(),
                    execution.getActivityInstanceId());
        }

        logUserTaskStart(execution);
    }

    protected void logUserTaskStart(DelegateExecution execution) {
        Date currTime = new Date();
        ServiceNodeStatusEntity entity = new ServiceNodeStatusEntity();
        entity.setCreatedBy(WorkflowConstants.DEFAULT_USER);
        entity.setCreatedTime(currTime);
        entity.setId(LocalIdGenerator.generateId());
        entity.setNodeId(execution.getCurrentActivityId());
        entity.setNodeInstId(execution.getActivityInstanceId());
        entity.setNodeName(execution.getCurrentActivityName());
        entity.setNodeType(NodeType.USER_TASK);
        entity.setProcInstKey(execution.getProcessBusinessKey());
        entity.setProcInstId(execution.getProcessInstanceId());
        entity.setStartTime(currTime);
        entity.setStatus(TraceStatus.InProgress);
        entity.setTryTimes(0);

        SpringApplicationContextUtil.getBean(ServiceNodeStatusMapper.class).insert(entity);
    }

}
