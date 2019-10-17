package com.webank.wecube.platform.core.service.workflow;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.dto.workflow.ProcessDefinitionInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeParamDto;
import com.webank.wecube.platform.core.entity.workflow.ProcessDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcessDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeParamRepository;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class WorkflowProcessDefinitionService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessDefinitionService.class);

    @Autowired
    private ProcessDefInfoRepository processDefInfoRepo;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepo;

    @Autowired
    private TaskNodeParamRepository taskNodeParamRepo;

    @Autowired
    private WorkflowService workflowService;

    public ProcessDefinitionInfoDto deployProcessDefinition(ProcessDefinitionInfoDto requestDto) {

        String originalId = requestDto.getId();

        ProcessDefInfoEntity draftEntity = null;
        if (!StringUtils.isBlank(originalId)) {
            Optional<ProcessDefInfoEntity> entityOpt = processDefInfoRepo.findById(originalId);
            if (entityOpt.isPresent()) {
                ProcessDefInfoEntity entity = entityOpt.get();
                log.info("entity is {}", entity);
                if (ProcessDefInfoEntity.DRAFT_STATUS.equals(entity.getStatus())) {
                    draftEntity = entity;
                }
            }
        }

        ProcessDefInfoEntity procEntity = new ProcessDefInfoEntity();
        procEntity.setId(LocalIdGenerator.generateId());
        procEntity.setProcDefData(requestDto.getProcDefData());
        procEntity.setProcDefKey(requestDto.getProcDefKey());
        procEntity.setRootEntity(requestDto.getRootEntity());
        procEntity.setStatus(ProcessDefInfoEntity.DEPLOYED_STATUS);

        processDefInfoRepo.save(procEntity);

        if (requestDto.getTaskNodeInfos() != null) {
            for (TaskNodeInfoDto nodeDto : requestDto.getTaskNodeInfos()) {
                TaskNodeDefInfoEntity nodeEntity = new TaskNodeDefInfoEntity();
                nodeEntity.setId(LocalIdGenerator.generateId());
                nodeEntity.setDescription(nodeDto.getDescription());
                nodeEntity.setNodeId(nodeDto.getNodeId());
                nodeEntity.setNodeName(nodeDto.getNodeName());
                nodeEntity.setProcDefId(procEntity.getId());
                nodeEntity.setProcDefKey(nodeDto.getProcessDefKey());
                nodeEntity.setRoutineExpression(nodeDto.getRoutineExpression());
                nodeEntity.setRoutineRaw(nodeDto.getRoutineRaw());
                nodeEntity.setServiceId(nodeDto.getServiceId());
                nodeEntity.setServiceName(nodeDto.getServiceName());
                nodeEntity.setStatus(TaskNodeDefInfoEntity.DEPLOYED_STATUS);

                taskNodeDefInfoRepo.save(nodeEntity);

                if (nodeDto.getParamInfos() != null) {
                    for (TaskNodeParamDto paramDto : nodeDto.getParamInfos()) {
                        TaskNodeParamEntity paramEntity = new TaskNodeParamEntity();
                        paramEntity.setId(LocalIdGenerator.generateId());
                        paramEntity.setNodeId(paramDto.getNodeId());
                        paramEntity.setParamExpression(paramDto.getParamExpression());
                        paramEntity.setParamName(paramDto.getParamName());
                        paramEntity.setProcDefId(procEntity.getId());
                        paramEntity.setStatus(TaskNodeParamEntity.DEPLOYED_STATUS);

                        taskNodeParamRepo.save(paramEntity);
                    }
                }
            }
        }

        ProcessDefinition procDef = workflowService.deployProcessDefinition(requestDto);

        // TODO to update entity info here

        ProcessDefinitionInfoDto result = new ProcessDefinitionInfoDto();
        result.setId(procEntity.getId());
        result.setProcDefData(requestDto.getProcDefData());
        result.setProcDefKey(procDef.getKey());
        result.setProcDefName(procDef.getName());
        result.setProcDefVersion(String.valueOf(procDef.getVersion()));
        result.setRootEntity(requestDto.getRootEntity());
        result.setStatus(procEntity.getStatus());
        //
        // result.setTaskNodeInfos(taskNodeInfos);

        return result;

    }

    public TaskNodeInfoDto configureTaskNode(String processId, TaskNodeInfoDto taskNode) {
        // if (StringUtils.isBlank(processId) || taskNode == null) {
        // throw new IllegalArgumentException();
        // }
        //
        // if (StringUtils.isBlank(taskNode.getNodeId())) {
        // throw new IllegalArgumentException("node id is blank.");
        // }
        //
        // ProcessDefInfoEntity draftProcDefInfo =
        // processDefInfoRepo.findOneByProcessIdAndStatus(processId,
        // ProcessDefInfoEntity.DRAFT_STATUS);
        //
        // if (draftProcDefInfo == null) {
        // draftProcDefInfo = new ProcessDefInfoEntity();
        // draftProcDefInfo.setId(LocalIdGenerator.generateId());
        // // draftProcDefInfo.setProcDefId(processId);
        // draftProcDefInfo.setStatus(ProcessDefInfoEntity.DRAFT_STATUS);
        //
        // draftProcDefInfo = processDefInfoRepo.save(draftProcDefInfo);
        // }
        //
        // TaskNodeDefInfoEntity draftTaskNodeEntity =
        // taskNodeDefInfoRepo.findOneWithProcessIdAndNodeIdAndStatus(
        // processId, taskNode.getNodeId(), TaskNodeDefInfoEntity.DRAFT_STATUS);
        //
        // if (draftTaskNodeEntity == null) {
        // draftTaskNodeEntity = new TaskNodeDefInfoEntity();
        // draftTaskNodeEntity.setId(LocalIdGenerator.generateId());
        // draftTaskNodeEntity.setProcDefId(processId);
        // // draftTaskNodeEntity.setTaskNodeId(taskNode.getNodeId());
        // draftTaskNodeEntity.setStatus(TaskNodeDefInfoEntity.DRAFT_STATUS);
        // }
        //
        // draftTaskNodeEntity = enrichTaskNodeDefInfoEntityFromDto(taskNode,
        // draftTaskNodeEntity);
        //
        // draftTaskNodeEntity = taskNodeDefInfoRepo.save(draftTaskNodeEntity);
        //
        // if (taskNode.getParamInfos() != null &&
        // !taskNode.getParamInfos().isEmpty()) {
        // for (TaskNodeParamDto paramDto : taskNode.getParamInfos()) {
        // TaskNodeParamEntity paramEntity = new TaskNodeParamEntity();
        // paramEntity.setId(LocalIdGenerator.generateId());
        //// paramEntity.setProcessId(processId);
        // paramEntity.setParamExpression(paramDto.getParamExpression());
        // paramEntity.setStatus(TaskNodeParamEntity.DRAFT_STATUS);
        // paramEntity.setParamName(paramDto.getParamName());
        //
        // taskNodeParamRepo.save(paramEntity);
        // }
        // }
        //
        // taskNode.setId(draftTaskNodeEntity.getId());
        // taskNode.setProcessId(draftTaskNodeEntity.getProcDefId());

        return taskNode;
    }

    private TaskNodeDefInfoEntity enrichTaskNodeDefInfoEntityFromDto(TaskNodeInfoDto dto,
            TaskNodeDefInfoEntity entity) {
        entity.setDescription(dto.getDescription());
        entity.setRoutineExpression(dto.getRoutineExpression());
        entity.setRoutineRaw(dto.getRoutineRaw());
        entity.setServiceId(dto.getServiceId());
        entity.setServiceName(dto.getServiceName());
        // entity.setTaksNodeName(dto.getNodeName());

        entity.setTimeoutExpression(dto.getTimeoutExpression());
        // entity.setTaskNodeType(dto.getNodeName());

        return entity;
    }

    public TaskNodeInfoDto getTaskNodeInfo(String processId, String nodeId) {
        if (StringUtils.isBlank(processId) || StringUtils.isBlank(nodeId)) {
            throw new IllegalArgumentException("process id or node id here cannot be blank.");
        }

        TaskNodeDefInfoEntity taskNodeEntity = taskNodeDefInfoRepo.findOneWithProcessIdAndNodeIdAndStatus(processId,
                nodeId, TaskNodeDefInfoEntity.DRAFT_STATUS);

        if (taskNodeEntity == null) {
            taskNodeEntity = taskNodeDefInfoRepo.findOneWithProcessIdAndNodeIdAndStatus(processId, nodeId,
                    TaskNodeDefInfoEntity.DEPLOYED_STATUS);
        }

        if (taskNodeEntity == null) {
            log.debug("didnot find task node entity with processId={},nodeId={}", processId, nodeId);
            return null;
        }

        TaskNodeInfoDto dto = new TaskNodeInfoDto();
        dto.setDescription(taskNodeEntity.getDescription());
        dto.setId(taskNodeEntity.getId());
        // dto.setNodeId(taskNodeEntity.getTaskNodeId());
        // dto.setNodeName(taskNodeEntity.getTaksNodeName());
        // TODO
        // dto.setParamInfos(paramInfos);

        // dto.setProcessId(taskNodeEntity.getProcDefId());
        dto.setRoutineExpression(taskNodeEntity.getRoutineExpression());
        dto.setRoutineRaw(taskNodeEntity.getRoutineRaw());
        dto.setServiceId(taskNodeEntity.getServiceId());
        dto.setServiceName(taskNodeEntity.getServiceName());
        dto.setStatus(taskNodeEntity.getStatus());
        dto.setTimeoutExpression(taskNodeEntity.getTimeoutExpression());
        return dto;
    }

}
