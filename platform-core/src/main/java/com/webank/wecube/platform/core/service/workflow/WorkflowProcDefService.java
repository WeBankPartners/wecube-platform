package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.FlowNodeDefDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefParamDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeParamRepository;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.model.ProcDefOutline;
import com.webank.wecube.platform.workflow.model.ProcFlowNode;
import com.webank.wecube.platform.workflow.parse.BpmnCustomizationException;

@Service
public class WorkflowProcDefService extends AbstractWorkflowService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcDefService.class);

    @Autowired
    private ProcDefInfoRepository processDefInfoRepo;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepo;

    @Autowired
    private TaskNodeParamRepository taskNodeParamRepo;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    public void removeProcessDefinition(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            throw new WecubeCoreException("Process definition id is blank.");
        }

        Optional<ProcDefInfoEntity> procDefOpt = processDefInfoRepo.findById(procDefId);

        if (!procDefOpt.isPresent()) {
            log.warn("such process definition does not exist:{}", procDefId);
            return;
        }

        ProcDefInfoEntity procDef = procDefOpt.get();

        if (!ProcDefInfoEntity.DRAFT_STATUS.equals(procDef.getStatus())) {
            throw new WecubeCoreException(
                    String.format("Such process definition under {%s} and cannot delete.", procDef.getStatus()));
        }

        List<TaskNodeParamEntity> nodeParams = taskNodeParamRepo.findAllByProcDefId(procDef.getId());

        if (nodeParams != null) {
            for (TaskNodeParamEntity p : nodeParams) {
                taskNodeParamRepo.delete(p);
            }
        }

        List<TaskNodeDefInfoEntity> nodeDefs = taskNodeDefInfoRepo.findAllByProcDefId(procDef.getId());
        if (nodeDefs != null) {
            for (TaskNodeDefInfoEntity n : nodeDefs) {
                taskNodeDefInfoRepo.delete(n);
            }
        }

        if (log.isInfoEnabled()) {
            log.info("process definition with id {} had been deleted successfully.", procDefId);
        }
        
        processDefInfoRepo.delete(procDef);
    }

    public List<TaskNodeDefBriefDto> getTaskNodeBriefs(String procDefId) {
        List<TaskNodeDefBriefDto> result = new ArrayList<>();
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.findAllByProcDefId(procDefId);
        if (nodeEntities == null || nodeEntities.isEmpty()) {
            return result;
        }

        nodeEntities.forEach(e -> {
            if (TaskNodeDefInfoEntity.NODE_TYPE_SUBPROCESS.equalsIgnoreCase(e.getNodeType())
                    || TaskNodeDefInfoEntity.NODE_TYPE_SERVICE_TASK.equalsIgnoreCase(e.getNodeType())) {
                TaskNodeDefBriefDto d = new TaskNodeDefBriefDto();
                d.setNodeDefId(e.getId());
                d.setNodeId(e.getNodeId());
                d.setNodeName(e.getNodeName());
                d.setNodeType(e.getNodeType());
                d.setProcDefId(e.getProcDefId());
                d.setServiceId(e.getServiceId());
                d.setServiceName(e.getServiceName());

                result.add(d);
            }
        });

        return result;

    }

    public ProcDefOutlineDto getProcessDefinitionOutline(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            throw new WecubeCoreException("Process definition ID is blank.");
        }

        Optional<ProcDefInfoEntity> procDefEntityOptional = processDefInfoRepo.findById(procDefId);
        if (!procDefEntityOptional.isPresent()) {
            log.debug("cannot find process def with id {}", procDefId);
            return null;
        }

        ProcDefInfoEntity procDefEntity = procDefEntityOptional.get();

        ProcDefOutlineDto result = new ProcDefOutlineDto();

        result.setProcDefId(procDefEntity.getId());
        result.setProcDefKey(procDefEntity.getProcDefKey());
        result.setProcDefName(procDefEntity.getProcDefName());
        result.setProcDefVersion(String.valueOf(procDefEntity.getProcDefVersion()));
        result.setRootEntity(procDefEntity.getRootEntity());
        result.setStatus(procDefEntity.getStatus());

        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.findAllByProcDefId(procDefEntity.getId());

        for (TaskNodeDefInfoEntity nodeEntity : nodeEntities) {
            FlowNodeDefDto fDto = flowNodeDefDtoFromEntity(nodeEntity);

            result.addFlowNodes(fDto);
        }

        return result;
    }
    
    private FlowNodeDefDto flowNodeDefDtoFromEntity(TaskNodeDefInfoEntity nodeEntity){
        FlowNodeDefDto fDto = new FlowNodeDefDto();
        fDto.setProcDefId(nodeEntity.getProcDefId());
        fDto.setProcDefKey(nodeEntity.getProcDefKey());
        fDto.setNodeId(nodeEntity.getNodeId());
        fDto.setNodeName(nodeEntity.getNodeName());
        fDto.setNodeType(nodeEntity.getNodeType());

        fDto.setNodeDefId(nodeEntity.getId());
        fDto.setStatus(nodeEntity.getStatus());
        fDto.setOrderedNo(nodeEntity.getOrderedNo());
        fDto.setRoutineExpression(nodeEntity.getRoutineExpression());

        List<String> previousNodeIds = unmarshalNodeIds(nodeEntity.getPreviousNodeIds());
        previousNodeIds.forEach(n -> fDto.addPreviousNodeIds(n));

        List<String> succeedingNodeIds = unmarshalNodeIds(nodeEntity.getSucceedingNodeIds());

        succeedingNodeIds.forEach(n -> fDto.addSucceedingNodeIds(n));
        
        return fDto;
    }

    public ProcDefInfoDto getProcessDefinition(String id) {
        Optional<ProcDefInfoEntity> procDefEntityOptional = processDefInfoRepo.findById(id);
        if (!procDefEntityOptional.isPresent()) {
            log.debug("cannot find process def with id {}", id);
            return null;
        }

        ProcDefInfoEntity procDefEntity = procDefEntityOptional.get();

        ProcDefInfoDto result = procDefInfoDtoFromEntity(procDefEntity);
        result.setProcDefData(procDefEntity.getProcDefData());

        List<TaskNodeDefInfoEntity> taskNodeDefEntities = taskNodeDefInfoRepo.findAllByProcDefId(id);
        for (TaskNodeDefInfoEntity e : taskNodeDefEntities) {
            TaskNodeDefInfoDto tdto = taskNodeDefInfoDtoFromEntity(e);

            List<TaskNodeParamEntity> taskNodeParamEntities = taskNodeParamRepo.findAllByProcDefIdAndTaskNodeDefId(id,
                    e.getId());

            for (TaskNodeParamEntity tnpe : taskNodeParamEntities) {
                TaskNodeDefParamDto pdto = taskNodeDefParamDtoFromEntity(tnpe);

                tdto.addParamInfos(pdto);
            }

            result.addTaskNodeInfo(tdto);
        }

        return result;
    }
    
    private ProcDefInfoDto procDefInfoDtoFromEntity(ProcDefInfoEntity procDefEntity){
        ProcDefInfoDto result = new ProcDefInfoDto();
        result.setProcDefId(procDefEntity.getId());
        result.setProcDefKey(procDefEntity.getProcDefKey());
        result.setProcDefName(procDefEntity.getProcDefName());
        result.setProcDefVersion(String.valueOf(procDefEntity.getProcDefVersion()));
        result.setRootEntity(procDefEntity.getRootEntity());
        result.setStatus(procDefEntity.getStatus());
//        result.setProcDefData(procDefEntity.getProcDefData());
        result.setCreatedTime(formatDate(procDefEntity.getCreatedTime()));
        
        return result;
    }
    
    private TaskNodeDefInfoDto taskNodeDefInfoDtoFromEntity(TaskNodeDefInfoEntity e){
        TaskNodeDefInfoDto tdto = new TaskNodeDefInfoDto();
        tdto.setDescription(e.getDescription());
        tdto.setNodeDefId(e.getId());
        tdto.setNodeId(e.getNodeId());
        tdto.setNodeName(e.getNodeName());
        tdto.setNodeType(e.getNodeType());
        tdto.setOrderedNo(e.getOrderedNo());
        tdto.setProcDefKey(e.getProcDefKey());
        tdto.setProcDefId(e.getProcDefId());
        tdto.setRoutineExpression(e.getRoutineExpression());
        tdto.setRoutineRaw(e.getRoutineRaw());
        tdto.setServiceId(e.getServiceId());
        tdto.setServiceName(e.getServiceName());
        tdto.setStatus(e.getStatus());
        tdto.setTimeoutExpression(e.getTimeoutExpression());
        
        return tdto;
    }
    
    private TaskNodeDefParamDto taskNodeDefParamDtoFromEntity(TaskNodeParamEntity tnpe){
        TaskNodeDefParamDto pdto = new TaskNodeDefParamDto();
        pdto.setId(tnpe.getId());
        pdto.setNodeId(tnpe.getNodeId());
        pdto.setParamName(tnpe.getParamName());
        pdto.setBindNodeId(tnpe.getBindNodeId());
        pdto.setBindParamName(tnpe.getBindParamName());
        pdto.setBindParamType(tnpe.getBindParamType());
        
        return pdto;
    }

    public List<ProcDefInfoDto> getProcessDefinitions(boolean includeDraftProcDef) {

        List<ProcDefInfoEntity> procDefEntities = null;
        if (includeDraftProcDef) {
            procDefEntities = processDefInfoRepo.findAllDeployedOrDraftProcDefs();
        } else {
            procDefEntities = processDefInfoRepo.findAllDeployedProcDefs();
        }

        if (procDefEntities == null) {
            return Collections.emptyList();
        }

        List<ProcDefInfoDto> procDefInfoDtos = new ArrayList<>();
        procDefEntities.forEach(e -> {
            ProcDefInfoDto dto = procDefInfoDtoFromEntity(e);
            procDefInfoDtos.add(dto);

        });
        return procDefInfoDtos;
    }

    public ProcDefInfoDto draftProcessDefinition(ProcDefInfoDto procDefDto) {
        String originalId = procDefDto.getProcDefId();

        ProcDefInfoEntity draftEntity = null;
        if (!StringUtils.isBlank(originalId)) {
            Optional<ProcDefInfoEntity> entityOpt = processDefInfoRepo.findById(originalId);
            if (entityOpt.isPresent()) {
                ProcDefInfoEntity entity = entityOpt.get();
                if (ProcDefInfoEntity.DRAFT_STATUS.equals(entity.getStatus())) {
                    draftEntity = entity;
                }
            } else {
                log.error("Invalid process definition id:{}", originalId);
                throw new WecubeCoreException("Invalid process definition id");
            }
        }

        if (draftEntity == null) {
            draftEntity = new ProcDefInfoEntity();
            draftEntity.setId(LocalIdGenerator.generateId());
            draftEntity.setStatus(ProcDefInfoEntity.DRAFT_STATUS);
        }

        draftEntity.setProcDefData(procDefDto.getProcDefData());
        draftEntity.setProcDefKey(procDefDto.getProcDefKey());
        draftEntity.setProcDefName(procDefDto.getProcDefName());
        draftEntity.setRootEntity(procDefDto.getRootEntity());
        draftEntity.setUpdatedTime(new Date());

        processDefInfoRepo.save(draftEntity);

        ProcDefInfoDto procDefResult = new ProcDefInfoDto();
        procDefResult.setProcDefId(draftEntity.getId());
        procDefResult.setProcDefData(draftEntity.getProcDefData());
        procDefResult.setProcDefKey(draftEntity.getProcDefKey());
        procDefResult.setProcDefName(draftEntity.getProcDefName());
        procDefResult.setRootEntity(draftEntity.getRootEntity());
        procDefResult.setStatus(draftEntity.getStatus());

        // TODO to save task nodes
        if (procDefDto.getTaskNodeInfos() != null) {
            for (TaskNodeDefInfoDto nodeDto : procDefDto.getTaskNodeInfos()) {
                String nodeOid = nodeDto.getNodeDefId();
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

                if (nodeDto.getParamInfos() != null && !nodeDto.getParamInfos().isEmpty()) {
                    for (TaskNodeDefParamDto nodeParamDto : nodeDto.getParamInfos()) {
                        String nodeParamOid = nodeParamDto.getId();
                        TaskNodeParamEntity draftNodeParamEntity = null;
                        if (!StringUtils.isBlank(nodeParamOid)) {
                            Optional<TaskNodeParamEntity> npEntityOptional = taskNodeParamRepo.findById(nodeParamOid);
                            if (npEntityOptional.isPresent()) {
                                TaskNodeParamEntity npEntity = npEntityOptional.get();
                                if (TaskNodeParamEntity.DRAFT_STATUS.equals(npEntity.getStatus())) {
                                    draftNodeParamEntity = npEntity;
                                }
                            }
                        }

                        if (draftNodeParamEntity == null) {
                            draftNodeParamEntity = new TaskNodeParamEntity();
                            draftNodeParamEntity.setId(LocalIdGenerator.generateId());
                            draftNodeParamEntity.setStatus(TaskNodeParamEntity.DRAFT_STATUS);
                        }

                        draftNodeParamEntity.setNodeId(nodeParamDto.getNodeId());
                        draftNodeParamEntity.setBindNodeId(nodeParamDto.getBindNodeId());
                        draftNodeParamEntity.setBindParamName(nodeParamDto.getBindParamName());
                        draftNodeParamEntity.setBindParamType(nodeParamDto.getBindParamType());
                        draftNodeParamEntity.setParamName(nodeParamDto.getParamName());
                        draftNodeParamEntity.setProcDefId(draftEntity.getId());
                        draftNodeParamEntity.setTaskNodeDefId(draftNodeEntity.getId());
                        draftNodeParamEntity.setUpdatedTime(new Date());

                        taskNodeParamRepo.save(draftNodeParamEntity);

                        // TODO
                    }
                }

                TaskNodeDefInfoDto nodeDtoResult = new TaskNodeDefInfoDto();
                nodeDtoResult.setNodeDefId(draftNodeEntity.getId());
                nodeDtoResult.setNodeId(draftNodeEntity.getNodeId());
                nodeDtoResult.setNodeName(draftNodeEntity.getNodeName());
                nodeDtoResult.setStatus(draftNodeEntity.getStatus());
                // TODO

                procDefResult.addTaskNodeInfo(nodeDtoResult);

            }
        }

        return procDefResult;
    }

    public ProcDefOutlineDto deployProcessDefinition(ProcDefInfoDto procDefInfoDto) {

        String originalId = procDefInfoDto.getProcDefId();

        ProcDefInfoEntity draftProcDefEntity = null;
        if (!StringUtils.isBlank(originalId)) {
            Optional<ProcDefInfoEntity> entityOpt = processDefInfoRepo.findById(originalId);
            if (entityOpt.isPresent()) {
                ProcDefInfoEntity entity = entityOpt.get();
                if (ProcDefInfoEntity.DRAFT_STATUS.equals(entity.getStatus())) {
                    draftProcDefEntity = entity;
                }
            }
        }

        ProcDefInfoEntity procDefEntity = new ProcDefInfoEntity();
        procDefEntity.setId(LocalIdGenerator.generateId());
        procDefEntity.setProcDefData(procDefInfoDto.getProcDefData());
        procDefEntity.setProcDefKey(procDefInfoDto.getProcDefKey());
        procDefEntity.setRootEntity(procDefInfoDto.getRootEntity());
        procDefEntity.setStatus(ProcDefInfoEntity.PREDEPLOY_STATUS);
        procDefEntity.setUpdatedTime(new Date());

        processDefInfoRepo.save(procDefEntity);

        if (procDefInfoDto.getTaskNodeInfos() != null) {
            for (TaskNodeDefInfoDto nodeDto : procDefInfoDto.getTaskNodeInfos()) {
                TaskNodeDefInfoEntity nodeEntity = new TaskNodeDefInfoEntity();
                nodeEntity.setId(LocalIdGenerator.generateId());
                nodeEntity.setDescription(nodeDto.getDescription());
                nodeEntity.setNodeId(nodeDto.getNodeId());
                nodeEntity.setNodeName(nodeDto.getNodeName());
                nodeEntity.setProcDefId(procDefEntity.getId());
                nodeEntity.setProcDefKey(nodeDto.getProcDefKey());
                nodeEntity.setRoutineExpression(nodeDto.getRoutineExpression());
                nodeEntity.setRoutineRaw(nodeDto.getRoutineRaw());
                nodeEntity.setServiceId(nodeDto.getServiceId());
                nodeEntity.setServiceName(nodeDto.getServiceName());
                nodeEntity.setStatus(TaskNodeDefInfoEntity.PREDEPLOY_STATUS);
                nodeEntity.setUpdatedTime(new Date());
                nodeEntity.setTimeoutExpression(nodeDto.getTimeoutExpression());

                taskNodeDefInfoRepo.save(nodeEntity);

                if (nodeDto.getParamInfos() != null) {
                    for (TaskNodeDefParamDto paramDto : nodeDto.getParamInfos()) {
                        TaskNodeParamEntity paramEntity = new TaskNodeParamEntity();
                        paramEntity.setId(LocalIdGenerator.generateId());
                        paramEntity.setNodeId(paramDto.getNodeId());
                        paramEntity.setBindNodeId(paramDto.getBindNodeId());
                        paramEntity.setBindParamName(paramDto.getBindParamName());
                        paramEntity.setBindParamType(paramDto.getBindParamType());
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
            procDef = workflowEngineService.deployProcessDefinition(procDefInfoDto);
        } catch (BpmnCustomizationException e) {
            log.error("failed to deploy process definition,msg={}", e.getMessage());
            deployFailed = true;
            handleDeployFailure(procDefEntity);
        }

        if (deployFailed || procDef == null) {
            throw new WecubeCoreException("Failed to deploy process definition.");
        }

        if (draftProcDefEntity != null) {
            purgeProcessDefInfoEntity(draftProcDefEntity);
        }

        ProcDefOutline procDefOutline = workflowEngineService.getProcDefOutline(procDef);

        return postDeployProcessDefinition(procDefEntity, procDef, procDefOutline);

    }

    protected ProcDefOutlineDto postDeployProcessDefinition(ProcDefInfoEntity procDefEntity, ProcessDefinition procDef,
            ProcDefOutline procDefOutline) {
        if (procDefEntity == null) {
            return null;
        }

        Date now = new Date();
        procDefEntity.setProcDefKernelId(procDef.getId());
        procDefEntity.setProcDefKey(procDef.getKey());
        procDefEntity.setProcDefName(procDef.getName());
        procDefEntity.setProcDefVersion(procDef.getVersion());
        procDefEntity.setStatus(ProcDefInfoEntity.DEPLOYED_STATUS);
        procDefEntity.setUpdatedTime(now);

        processDefInfoRepo.save(procDefEntity);

        ProcDefOutlineDto result = new ProcDefOutlineDto();
        result.setProcDefId(procDefEntity.getId());
        result.setProcDefKey(procDefEntity.getProcDefKey());
        result.setProcDefName(procDefEntity.getProcDefName());
        result.setProcDefVersion(String.valueOf(procDef.getVersion()));
        result.setRootEntity(procDefEntity.getRootEntity());
        result.setStatus(procDefEntity.getStatus());

        List<TaskNodeParamEntity> nodeParamEntities = taskNodeParamRepo.findAllByProcDefId(procDefEntity.getId());
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.findAllByProcDefId(procDefEntity.getId());

        final AtomicInteger orderedNo = new AtomicInteger(1);

        for (ProcFlowNode pfn : procDefOutline.getFlowNodes()) {
            TaskNodeDefInfoEntity nodeEntity = findNodeEntityByNodeId(nodeEntities, pfn.getId());
            if (nodeEntity == null) {
                log.debug("did not find such task node configuration and create new one,procDefId={},nodeId={}",
                        procDefEntity.getId(), pfn.getId());
                nodeEntity = new TaskNodeDefInfoEntity();
                nodeEntity.setId(LocalIdGenerator.generateId());
                nodeEntity.setProcDefId(procDefEntity.getId());
                nodeEntity.setProcDefKey(procDef.getKey());
                nodeEntity.setNodeId(pfn.getId());
            } else {
                nodeEntity.setUpdatedTime(now);
            }
            nodeEntity.setNodeName(pfn.getNodeName());
            nodeEntity.setNodeType(pfn.getNodeType());
            nodeEntity.setStatus(TaskNodeDefInfoEntity.DEPLOYED_STATUS);
            nodeEntity.setProcDefKernelId(procDef.getId());
            nodeEntity.setProcDefKey(procDef.getKey());
            nodeEntity.setProcDefVersion(procDef.getVersion());
            if ("subProcess".equals(pfn.getNodeType()) || "serviceTask".equals(pfn.getNodeType())) {
                nodeEntity.setOrderedNo(String.valueOf(orderedNo.getAndIncrement()));
            }
            nodeEntity.setPreviousNodeIds(marshalNodeIds(pfn.getPreviousFlowNodes()));
            nodeEntity.setSucceedingNodeIds(marshalNodeIds(pfn.getSucceedingFlowNodes()));
            taskNodeDefInfoRepo.save(nodeEntity);

            FlowNodeDefDto nodeDefDto = result.findFlowNodeDefDto(pfn.getId());

            if (nodeDefDto == null) {
                nodeDefDto = new FlowNodeDefDto();
                nodeDefDto.setProcDefKey(procDefEntity.getProcDefKey());
                nodeDefDto.setNodeId(pfn.getId());
                nodeDefDto.setNodeName(pfn.getNodeName());
                nodeDefDto.setNodeType(pfn.getNodeType());
            }

            for (ProcFlowNode pf : pfn.getPreviousFlowNodes()) {
                nodeDefDto.addPreviousNodeIds(pf.getId());
            }

            for (ProcFlowNode sf : pfn.getSucceedingFlowNodes()) {
                nodeDefDto.addSucceedingNodeIds(sf.getId());
            }

            nodeDefDto.setProcDefId(nodeEntity.getProcDefId());
            nodeDefDto.setNodeDefId(nodeEntity.getId());
            nodeDefDto.setOrderedNo(String.valueOf(nodeEntity.getOrderedNo()));
            nodeDefDto.setStatus(nodeEntity.getStatus());

            result.addFlowNodes(nodeDefDto);
        }

        if (nodeParamEntities != null && !nodeParamEntities.isEmpty()) {
            nodeParamEntities.forEach(n -> {
                n.setUpdatedTime(now);
                n.setStatus(TaskNodeParamEntity.DEPLOYED_STATUS);
                taskNodeParamRepo.save(n);
            });
        }

        return result;
    }

    protected String marshalNodeIds(List<ProcFlowNode> flowNodes) {
        if (flowNodes == null || flowNodes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (ProcFlowNode f : flowNodes) {
            sb.append(f.getId()).append(NODE_IDS_DELIMITER);
        }

        return sb.toString();
    }

    private TaskNodeDefInfoEntity findNodeEntityByNodeId(List<TaskNodeDefInfoEntity> nodeEntities, String nodeId) {
        if (nodeEntities == null) {
            return null;
        }
        for (TaskNodeDefInfoEntity entity : nodeEntities) {
            if (nodeId.equals(entity.getNodeId())) {
                return entity;
            }
        }

        return null;
    }

    protected void handleDeployFailure(ProcDefInfoEntity procEntity) {
        if (procEntity == null) {
            return;
        }
        purgeProcessDefInfoEntity(procEntity);
    }

    protected void purgeProcessDefInfoEntity(ProcDefInfoEntity procEntity) {
        List<TaskNodeParamEntity> nodeParamEntities = taskNodeParamRepo.findAllByProcDefId(procEntity.getId());
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.findAllByProcDefId(procEntity.getId());

        if (nodeParamEntities != null && !nodeParamEntities.isEmpty()) {
            taskNodeParamRepo.deleteAll(nodeParamEntities);
        }

        if (nodeEntities != null && !nodeEntities.isEmpty()) {
            taskNodeDefInfoRepo.deleteAll(nodeEntities);
        }

        processDefInfoRepo.deleteById(procEntity.getId());
    }

}
