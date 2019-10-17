package com.webank.wecube.platform.core.service.workflow;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.dto.workflow.TaskNodeInfoDto;
import com.webank.wecube.platform.core.entity.workflow.ProcessDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcessDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;

@Service
public class WorkflowProcessDefinitionService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessDefinitionService.class);

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepo;

    @Autowired
    private ProcessDefInfoRepository processDefInfoRepo;

    public TaskNodeInfoDto configureTaskNode(String processId, TaskNodeInfoDto taskNode) {
        if (StringUtils.isBlank(processId) || taskNode == null) {
            throw new IllegalArgumentException();
        }

        if (StringUtils.isBlank(taskNode.getNodeId())) {
            throw new IllegalArgumentException("node id is blank.");
        }

        ProcessDefInfoEntity draftProcDefInfo = processDefInfoRepo.findOneByProcessIdAndStatus(processId,
                ProcessDefInfoEntity.DRAFT_STATUS);

        if (draftProcDefInfo == null) {
            draftProcDefInfo = new ProcessDefInfoEntity();
            draftProcDefInfo.setProcDefId(processId);
            draftProcDefInfo.setStatus(ProcessDefInfoEntity.DRAFT_STATUS);

            draftProcDefInfo = processDefInfoRepo.save(draftProcDefInfo);
        }

        TaskNodeDefInfoEntity draftTaskNodeEntity = taskNodeDefInfoRepo.findOneWithProcessIdAndNodeIdAndStatus(
                processId, taskNode.getNodeId(), TaskNodeDefInfoEntity.DRAFT_STATUS);

        if (draftTaskNodeEntity == null) {
            draftTaskNodeEntity = new TaskNodeDefInfoEntity();
            draftTaskNodeEntity.setProcDefId(processId);
            draftTaskNodeEntity.setTaskNodeId(taskNode.getNodeId());
            draftTaskNodeEntity.setStatus(TaskNodeDefInfoEntity.DRAFT_STATUS);
        }

        draftTaskNodeEntity = enrichTaskNodeDefInfoEntityFromDto(taskNode, draftTaskNodeEntity);

        draftTaskNodeEntity = taskNodeDefInfoRepo.save(draftTaskNodeEntity);

        taskNode.setId(draftTaskNodeEntity.getId());
        taskNode.setProcessId(draftTaskNodeEntity.getProcDefId());

        return taskNode;
    }

    private TaskNodeDefInfoEntity enrichTaskNodeDefInfoEntityFromDto(TaskNodeInfoDto dto,
            TaskNodeDefInfoEntity entity) {
        entity.setDescription(dto.getDescription());
        entity.setRoutineExpression(dto.getRoutineExpression());
        entity.setRoutineRaw(dto.getRoutineRaw());
        entity.setServiceId(dto.getServiceId());
        entity.setServiceName(dto.getServiceName());
        entity.setTaksNodeName(dto.getNodeName());

        entity.setTimeoutExpression(dto.getTimeoutExpression());
        entity.setTaskNodeType(dto.getNodeName());

        return entity;
    }

    public TaskNodeInfoDto getTaskNodeInfo(String processId, String nodeId) {
        if (StringUtils.isBlank(processId) || StringUtils.isBlank(nodeId)) {
            throw new IllegalArgumentException("process id or node id here cannot be blank.");
        }

        TaskNodeDefInfoEntity taskNodeEntity = taskNodeDefInfoRepo.findOneWithProcessIdAndNodeIdAndStatus(processId,
                nodeId, TaskNodeDefInfoEntity.DRAFT_STATUS);
        
        if(taskNodeEntity == null){
            taskNodeEntity = taskNodeDefInfoRepo.findOneWithProcessIdAndNodeIdAndStatus(processId,
                    nodeId, TaskNodeDefInfoEntity.DEPLOYED_STATUS);
        }
        
        if(taskNodeEntity == null){
            return null;
        }
        
        
        return null;
    }

}
