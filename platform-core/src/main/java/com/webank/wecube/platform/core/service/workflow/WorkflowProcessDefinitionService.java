package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    
    public ProcessDefinitionInfoDto getProcessDefinition(String id){
        Optional<ProcessDefInfoEntity> procDefEntityOptional = processDefInfoRepo.findById(id);
        if(!procDefEntityOptional.isPresent()){
            log.debug("cannot find process def with id {}", id);
            return null;
        }
        
        ProcessDefInfoEntity procDefEntity = procDefEntityOptional.get();
        
        ProcessDefinitionInfoDto dto = new ProcessDefinitionInfoDto();
        dto.setId(procDefEntity.getId());
        dto.setProcDefKey(procDefEntity.getProcDefKey());
        dto.setProcDefName(procDefEntity.getProcDefName());
        dto.setProcDefVersion(String.valueOf(procDefEntity.getProcDefVersion()));
        dto.setRootEntity(procDefEntity.getRootEntity());
        dto.setStatus(procDefEntity.getStatus());
        dto.setProcDefData(procDefEntity.getProcDefData());
        
        return dto;
    }
    
    public List<ProcessDefinitionInfoDto> getProcessDefinitions(boolean includeDraftProcDef){
        
        List<ProcessDefInfoEntity> procDefEntities = processDefInfoRepo.findAllDeployedOrDraftProcDef();
        if(procDefEntities == null){
            return Collections.emptyList();
        }
        
        List<ProcessDefinitionInfoDto> procDefInfoDtos = new ArrayList<>();
        procDefEntities.forEach(e -> {
            ProcessDefinitionInfoDto dto = new ProcessDefinitionInfoDto();
            dto.setId(e.getId());
            dto.setProcDefKey(e.getProcDefKey());
            dto.setProcDefName(e.getProcDefName());
            dto.setProcDefVersion(String.valueOf(e.getProcDefVersion()));
            dto.setRootEntity(e.getRootEntity());
            dto.setStatus(e.getStatus());
            
            procDefInfoDtos.add(dto);
            
        });
        return procDefInfoDtos;
    }
    
    public ProcessDefinitionInfoDto draftProcessDefinition(ProcessDefinitionInfoDto procDefInfoDto){
        return null;
    }

    public ProcessDefinitionInfoDto deployProcessDefinition(ProcessDefinitionInfoDto procDefInfoDto) {

        String originalId = procDefInfoDto.getId();

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
        procEntity.setProcDefData(procDefInfoDto.getProcDefData());
        procEntity.setProcDefKey(procDefInfoDto.getProcDefKey());
        procEntity.setRootEntity(procDefInfoDto.getRootEntity());
        procEntity.setStatus(ProcessDefInfoEntity.DEPLOYED_STATUS);

        processDefInfoRepo.save(procEntity);

        if (procDefInfoDto.getTaskNodeInfos() != null) {
            for (TaskNodeInfoDto nodeDto : procDefInfoDto.getTaskNodeInfos()) {
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

        ProcessDefinition procDef = workflowService.deployProcessDefinition(procDefInfoDto);

        // TODO to update entity info here

        ProcessDefinitionInfoDto result = new ProcessDefinitionInfoDto();
        result.setId(procEntity.getId());
        result.setProcDefData(procDefInfoDto.getProcDefData());
        result.setProcDefKey(procDef.getKey());
        result.setProcDefName(procDef.getName());
        result.setProcDefVersion(String.valueOf(procDef.getVersion()));
        result.setRootEntity(procDefInfoDto.getRootEntity());
        result.setStatus(procEntity.getStatus());
        //
        // result.setTaskNodeInfos(taskNodeInfos);
        
        //TODO to handle draft

        return result;

    }

    

   

    

}
