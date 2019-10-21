package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
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
import com.webank.wecube.platform.workflow.parse.BpmnCustomizationException;

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
    private WorkflowEngineService workflowService;

    public ProcessDefinitionInfoDto getProcessDefinition(String id) {
        Optional<ProcessDefInfoEntity> procDefEntityOptional = processDefInfoRepo.findById(id);
        if (!procDefEntityOptional.isPresent()) {
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

    public List<ProcessDefinitionInfoDto> getProcessDefinitions(boolean includeDraftProcDef) {

        List<ProcessDefInfoEntity> procDefEntities = processDefInfoRepo.findAllDeployedOrDraftProcDef();
        if (procDefEntities == null) {
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

    public ProcessDefinitionInfoDto draftProcessDefinition(ProcessDefinitionInfoDto procDefDto) {
        String originalId = procDefDto.getId();

        ProcessDefInfoEntity draftEntity = null;
        if (!StringUtils.isBlank(originalId)) {
            Optional<ProcessDefInfoEntity> entityOpt = processDefInfoRepo.findById(originalId);
            if (entityOpt.isPresent()) {
                ProcessDefInfoEntity entity = entityOpt.get();
                if (ProcessDefInfoEntity.DRAFT_STATUS.equals(entity.getStatus())) {
                    draftEntity = entity;
                }
            } else {
                throw new WecubeCoreException("Invalid process definition id");
            }
        }

        if (draftEntity == null) {
            draftEntity = new ProcessDefInfoEntity();
            draftEntity.setId(LocalIdGenerator.generateId());
            draftEntity.setStatus(ProcessDefInfoEntity.DRAFT_STATUS);
        }

        draftEntity.setProcDefData(procDefDto.getProcDefData());
        draftEntity.setProcDefKey(procDefDto.getProcDefKey());
        draftEntity.setProcDefName(procDefDto.getProcDefName());
        draftEntity.setRootEntity(procDefDto.getRootEntity());
        draftEntity.setUpdatedTime(new Date());

        processDefInfoRepo.save(draftEntity);

        ProcessDefinitionInfoDto procDefResult = new ProcessDefinitionInfoDto();
        procDefResult.setId(draftEntity.getId());
        procDefResult.setProcDefData(procDefDto.getProcDefData());
        procDefResult.setProcDefKey(draftEntity.getProcDefKey());
        procDefResult.setProcDefName(draftEntity.getProcDefName());
        procDefResult.setRootEntity(draftEntity.getRootEntity());
        procDefResult.setStatus(draftEntity.getStatus());

        // TODO to save task nodes
        if (procDefDto.getTaskNodeInfos() != null) {
            for (TaskNodeInfoDto nodeDto : procDefDto.getTaskNodeInfos()) {
                String nodeOid = nodeDto.getId();
                TaskNodeDefInfoEntity draftNodeEntity = null;
                if (!StringUtils.isBlank(nodeOid)) {
                    Optional<TaskNodeDefInfoEntity> nEntityOpt = taskNodeDefInfoRepo.findById(nodeOid);
                    if (nEntityOpt.isPresent()) {
                        TaskNodeDefInfoEntity nEntity = nEntityOpt.get();
                        if (TaskNodeDefInfoEntity.DRAFT_STATUS.equals(nEntity.getStatus())) {
                            draftNodeEntity = nEntity;
                        }
                    }
                }

                if (draftNodeEntity == null) {
                    draftNodeEntity = new TaskNodeDefInfoEntity();
                    draftNodeEntity.setId(LocalIdGenerator.generateId());
                    draftNodeEntity.setStatus(TaskNodeDefInfoEntity.DRAFT_STATUS);
                }

                draftNodeEntity.setDescription(nodeDto.getDescription());
                draftNodeEntity.setNodeId(nodeDto.getNodeId());
                draftNodeEntity.setNodeName(nodeDto.getNodeName());
                draftNodeEntity.setProcDefId(draftEntity.getId());
                draftNodeEntity.setProcDefKey(draftEntity.getProcDefKey());
                draftNodeEntity.setRoutineExpression(nodeDto.getRoutineExpression());
                draftNodeEntity.setRoutineRaw(nodeDto.getRoutineRaw());
                draftNodeEntity.setServiceId(nodeDto.getServiceId());
                draftNodeEntity.setServiceName(nodeDto.getServiceName());
                draftNodeEntity.setTimeoutExpression(nodeDto.getTimeoutExpression());
                draftNodeEntity.setUpdatedTime(new Date());

                taskNodeDefInfoRepo.save(draftNodeEntity);
                
                if(nodeDto.getParamInfos() != null && !nodeDto.getParamInfos().isEmpty()){
                    for(TaskNodeParamDto nodeParamDto : nodeDto.getParamInfos()){
                        String nodeParamOid = nodeParamDto.getId();
                        TaskNodeParamEntity draftNodeParamEntity = null;
                        if(!StringUtils.isBlank(nodeParamOid)){
                            Optional<TaskNodeParamEntity> npEntityOptional = taskNodeParamRepo.findById(nodeParamOid);
                            if(npEntityOptional.isPresent()){
                                TaskNodeParamEntity npEntity = npEntityOptional.get();
                                if(TaskNodeParamEntity.DRAFT_STATUS.equals(npEntity.getStatus())){
                                    draftNodeParamEntity =  npEntity;
                                }
                            }
                        }
                        
                        if(draftNodeParamEntity == null){
                            draftNodeParamEntity = new TaskNodeParamEntity();
                            draftNodeParamEntity.setId(LocalIdGenerator.generateId());
                            draftNodeParamEntity.setStatus(TaskNodeParamEntity.DRAFT_STATUS);
                        }
                        
                        draftNodeParamEntity.setNodeId(nodeParamDto.getNodeId());
                        draftNodeParamEntity.setParamExpression(nodeParamDto.getParamExpression());
                        draftNodeParamEntity.setParamName(nodeParamDto.getParamName());
                        draftNodeParamEntity.setProcDefId(draftEntity.getId());
                        draftNodeParamEntity.setTaskNodeDefId(draftNodeEntity.getId());
                        draftNodeParamEntity.setUpdatedTime(new Date());
                        
                        taskNodeParamRepo.save(draftNodeParamEntity);
                        
                        //TODO
                    }
                }

                TaskNodeInfoDto nodeDtoResult = new TaskNodeInfoDto();
                nodeDtoResult.setId(draftNodeEntity.getId());
                nodeDtoResult.setNodeId(draftNodeEntity.getNodeId());
                nodeDtoResult.setNodeName(draftNodeEntity.getNodeName());
                nodeDtoResult.setStatus(draftNodeEntity.getStatus());
                // TODO

                procDefResult.addTaskNodeInfo(nodeDtoResult);

            }
        }

        return procDefResult;
    }

    public ProcessDefinitionInfoDto deployProcessDefinition(ProcessDefinitionInfoDto procDefInfoDto) {

        String originalId = procDefInfoDto.getId();

        ProcessDefInfoEntity draftProcDefEntity = null;
        if (!StringUtils.isBlank(originalId)) {
            Optional<ProcessDefInfoEntity> entityOpt = processDefInfoRepo.findById(originalId);
            if (entityOpt.isPresent()) {
                ProcessDefInfoEntity entity = entityOpt.get();
                if (ProcessDefInfoEntity.DRAFT_STATUS.equals(entity.getStatus())) {
                    draftProcDefEntity = entity;
                }
            }
        }

        ProcessDefInfoEntity procDefEntity = new ProcessDefInfoEntity();
        procDefEntity.setId(LocalIdGenerator.generateId());
        procDefEntity.setProcDefData(procDefInfoDto.getProcDefData());
        procDefEntity.setProcDefKey(procDefInfoDto.getProcDefKey());
        procDefEntity.setRootEntity(procDefInfoDto.getRootEntity());
        procDefEntity.setStatus(ProcessDefInfoEntity.PREDEPLOY_STATUS);
        procDefEntity.setUpdatedTime(new Date());

        processDefInfoRepo.save(procDefEntity);

        if (procDefInfoDto.getTaskNodeInfos() != null) {
            for (TaskNodeInfoDto nodeDto : procDefInfoDto.getTaskNodeInfos()) {
                TaskNodeDefInfoEntity nodeEntity = new TaskNodeDefInfoEntity();
                nodeEntity.setId(LocalIdGenerator.generateId());
                nodeEntity.setDescription(nodeDto.getDescription());
                nodeEntity.setNodeId(nodeDto.getNodeId());
                nodeEntity.setNodeName(nodeDto.getNodeName());
                nodeEntity.setProcDefId(procDefEntity.getId());
                nodeEntity.setProcDefKey(nodeDto.getProcessDefKey());
                nodeEntity.setRoutineExpression(nodeDto.getRoutineExpression());
                nodeEntity.setRoutineRaw(nodeDto.getRoutineRaw());
                nodeEntity.setServiceId(nodeDto.getServiceId());
                nodeEntity.setServiceName(nodeDto.getServiceName());
                nodeEntity.setStatus(TaskNodeDefInfoEntity.PREDEPLOY_STATUS);
                nodeEntity.setUpdatedTime(new Date());

                taskNodeDefInfoRepo.save(nodeEntity);

                if (nodeDto.getParamInfos() != null) {
                    for (TaskNodeParamDto paramDto : nodeDto.getParamInfos()) {
                        TaskNodeParamEntity paramEntity = new TaskNodeParamEntity();
                        paramEntity.setId(LocalIdGenerator.generateId());
                        paramEntity.setNodeId(paramDto.getNodeId());
                        paramEntity.setParamExpression(paramDto.getParamExpression());
                        paramEntity.setParamName(paramDto.getParamName());
                        paramEntity.setProcDefId(procDefEntity.getId());
                        paramEntity.setStatus(TaskNodeParamEntity.PREDEPLOY_STATUS);
                        paramEntity.setUpdatedTime(new Date());

                        taskNodeParamRepo.save(paramEntity);
                    }
                }
            }
        }

        ProcessDefinition procDef = null;
        boolean deployFailed = false;
        try {
            procDef = workflowService.deployProcessDefinition(procDefInfoDto);
        } catch (BpmnCustomizationException e) {
            log.error("failed to deploy process definition,msg={}", e.getMessage());
            deployFailed = true;
            handleDeployFailure(procDefEntity);
        }
        
        
        
        if(deployFailed || procDef == null){
            throw new WecubeCoreException("Failed to deploy process definition.");
        }
        
        if (draftProcDefEntity != null) {
            purgeProcessDefInfoEntity(draftProcDefEntity);
        }
        
        postDeployProcessDefinition(procDefEntity, procDef);


        ProcessDefinitionInfoDto result = new ProcessDefinitionInfoDto();
        result.setId(procDefEntity.getId());
        result.setProcDefData(procDefInfoDto.getProcDefData());
        result.setProcDefKey(procDef.getKey());
        result.setProcDefName(procDef.getName());
        result.setProcDefVersion(String.valueOf(procDef.getVersion()));
        result.setRootEntity(procDefInfoDto.getRootEntity());
        result.setStatus(procDefEntity.getStatus());
        //
        // result.setTaskNodeInfos(taskNodeInfos);

        return result;

    }
    
    protected void postDeployProcessDefinition(ProcessDefInfoEntity procDefEntity,ProcessDefinition procDef){
        if(procDefEntity == null){
            return;
        }
        
        Date now = new Date();
        procDefEntity.setProcDefKernelId(procDef.getId());
        procDefEntity.setProcDefKey(procDef.getKey());
        procDefEntity.setProcDefName(procDef.getName());
        procDefEntity.setProcDefVersion(procDef.getVersion());
        procDefEntity.setStatus(ProcessDefInfoEntity.DEPLOYED_STATUS);
        procDefEntity.setUpdatedTime(now);
        
        processDefInfoRepo.save(procDefEntity);
        
        List<TaskNodeParamEntity> nodeParamEntities = taskNodeParamRepo.findAllByProcDefId(procDefEntity.getId());
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.findAllByProcDefId(procDefEntity.getId());
        
        if(nodeEntities != null && !nodeEntities.isEmpty()){
            nodeEntities.forEach(n -> {
                n.setUpdatedTime(now);
                n.setProcDefKernelId(procDef.getId());
                n.setProcDefKey(procDef.getKey());
                n.setProcDefVersion(procDef.getVersion());
                n.setStatus(TaskNodeDefInfoEntity.DEPLOYED_STATUS);
                taskNodeDefInfoRepo.save(n);
            });
        }
        
        if(nodeParamEntities != null && !nodeParamEntities.isEmpty()){
            nodeParamEntities.forEach(n ->  {
                n.setUpdatedTime(now);
                n.setStatus(TaskNodeParamEntity.DEPLOYED_STATUS);
                taskNodeParamRepo.save(n);
            });
        }
        
    }
    
    protected void handleDeployFailure(ProcessDefInfoEntity procEntity){
        if(procEntity == null){
            return;
        }
        purgeProcessDefInfoEntity(procEntity);
    }
    
    protected void purgeProcessDefInfoEntity(ProcessDefInfoEntity procEntity){
        List<TaskNodeParamEntity> nodeParamEntities = taskNodeParamRepo.findAllByProcDefId(procEntity.getId());
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.findAllByProcDefId(procEntity.getId());
        
        if(nodeParamEntities != null && !nodeParamEntities.isEmpty()){
            taskNodeParamRepo.deleteAll(nodeParamEntities);
        }
        
        if(nodeEntities != null && !nodeEntities.isEmpty()){
            taskNodeDefInfoRepo.deleteAll(nodeEntities);
        }
        
        processDefInfoRepo.deleteById(procEntity.getId());
    }

}
