package com.webank.wecube.platform.core.service.workflow;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ContinueTokenInfoDto;
import com.webank.wecube.platform.core.dto.workflow.FlowNodeDefDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDeploymentResultDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefParamDto;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;
import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.utils.CollectionUtils;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;
import com.webank.wecube.platform.workflow.model.ProcDefOutline;
import com.webank.wecube.platform.workflow.model.ProcFlowNode;
import com.webank.wecube.platform.workflow.parse.BpmnCustomizationException;

/**
 * 
 * 
 *
 */
@Service
public class WorkflowProcDefDeploymentService extends AbstractWorkflowProcDefService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProcDefDeploymentService.class);
    
    /**
     * 
     * @param procDefDto
     * @return
     */
    public ProcDefInfoDto draftProcessDefinition(ProcDefInfoDto procDefDto) {
        String originalId = procDefDto.getProcDefId();

        Date currTime = new Date();
        String currUser = AuthenticationContextHolder.getCurrentUsername();

        ProcDefInfoEntity draftEntity = null;
        if (!StringUtils.isBlank(originalId)) {
            ProcDefInfoEntity entity = processDefInfoRepo.selectByPrimaryKey(originalId);
            if (entity != null) {
                if (ProcDefInfoEntity.DRAFT_STATUS.equals(entity.getStatus())) {
                    draftEntity = entity;
                }
            } else {
                log.warn("Invalid process definition id:{}", originalId);
                throw new WecubeCoreException("3207", "Invalid process definition id");
            }
        }

        if (draftEntity == null) {
            draftEntity = new ProcDefInfoEntity();
            draftEntity.setId(LocalIdGenerator.generateId());
            draftEntity.setStatus(ProcDefInfoEntity.DRAFT_STATUS);
            draftEntity.setCreatedBy(currUser);
            draftEntity.setCreatedTime(currTime);

            processDefInfoRepo.insert(draftEntity);
        }

        draftEntity.setProcDefData(procDefDto.getProcDefData());
        draftEntity.setProcDefKey(procDefDto.getProcDefKey());
        draftEntity.setProcDefName(procDefDto.getProcDefName());
        draftEntity.setRootEntity(procDefDto.getRootEntity());
        draftEntity.setUpdatedTime(currTime);
        draftEntity.setUpdatedBy(currUser);
        draftEntity.setExcludeMode(procDefDto.getExcludeMode());
        draftEntity.setTags(procDefDto.getTags());

        processDefInfoRepo.updateByPrimaryKeySelective(draftEntity);
        // Save ProcRoleBindingEntity
        this.saveProcRoleBinding(draftEntity.getId(), procDefDto);

        ProcDefInfoDto procDefResult = new ProcDefInfoDto();
        procDefResult.setProcDefId(draftEntity.getId());
        procDefResult.setProcDefData(draftEntity.getProcDefData());
        procDefResult.setProcDefKey(draftEntity.getProcDefKey());
        procDefResult.setProcDefName(draftEntity.getProcDefName());
        procDefResult.setRootEntity(draftEntity.getRootEntity());
        procDefResult.setStatus(draftEntity.getStatus());
        procDefResult.setExcludeMode(draftEntity.getExcludeMode());
        procDefResult.setTags(draftEntity.getTags());

        processDraftTaskNodeInfos(procDefDto, draftEntity, procDefResult, currTime);

        return procDefResult;
    }

    /**
     * 
     * @param procDefInfoDto
     * @param continueToken
     * @return
     */
    public ProcessDeploymentResultDto deployProcessDefinition(ProcDefInfoDto procDefInfoDto, String continueToken) {

        validateTaskInfos(procDefInfoDto);

        String procDefName = procDefInfoDto.getProcDefName();
        if (StringUtils.isBlank(procDefName)) {
            throw new WecubeCoreException("3208", "Process definition name cannot be empty.");
        }

        List<ProcDefInfoEntity> existingProcDefs = processDefInfoRepo
                .selectAllDeployedProcDefsByProcDefName(procDefName);
        if (existingProcDefs != null && !existingProcDefs.isEmpty()) {
            return tryPerformExistingProcessDeployment(existingProcDefs.get(0), procDefInfoDto, continueToken);
        }

        return tryPerformNewProcessDeployment(procDefInfoDto);

    }
    
    private void processDraftTaskNodeInfos(ProcDefInfoDto procDefDto, ProcDefInfoEntity draftEntity,
            ProcDefInfoDto procDefResult, Date currTime) {
        ProcDefOutline procDefOutline = workflowEngineService
                .readProcDefOutlineFromXmlData(procDefDto.getProcDefData());

        if (procDefDto.getTaskNodeInfos() == null) {
            log.debug("task node infos is null from input argument for process definition:{}",
                    procDefDto.getProcDefId());
            return;
        }

        TaskNodeDefInfoDto startEventNodeDto = null;
        String currUser = AuthenticationContextHolder.getCurrentUsername();

        // #1993
        for (TaskNodeDefInfoDto nodeDto : procDefDto.getTaskNodeInfos()) {
            String nodeOid = nodeDto.getNodeDefId();
            TaskNodeDefInfoEntity draftNodeEntity = tryFindDraftNodeEntity(nodeOid, draftEntity.getId(),
                    nodeDto.getNodeId());

            ProcFlowNode procFlowNode = procDefOutline.findFlowNode(nodeDto.getNodeId());
            if (procFlowNode == null) {
                log.info("task node {} {} is outdated ", nodeOid, nodeDto.getNodeId());
                tryClearOutDatedDraftNodeEntity(draftNodeEntity);
                continue;
            }

            if (NODE_START_EVENT.equals(procFlowNode.getNodeType())) {
                startEventNodeDto = nodeDto;
            }

            if (draftNodeEntity == null) {
                draftNodeEntity = new TaskNodeDefInfoEntity();
                draftNodeEntity.setId(LocalIdGenerator.generateId());
                draftNodeEntity.setStatus(TaskNodeDefInfoEntity.DRAFT_STATUS);
                draftNodeEntity.setCreatedBy(currUser);
                draftNodeEntity.setCreatedTime(currTime);
                taskNodeDefInfoRepo.insert(draftNodeEntity);
            }

            draftNodeEntity.setDescription(nodeDto.getDescription());
            draftNodeEntity.setNodeId(nodeDto.getNodeId());
            draftNodeEntity.setNodeName(procFlowNode.getNodeName());
            draftNodeEntity.setNodeType(procFlowNode.getNodeType());
            draftNodeEntity.setProcDefId(draftEntity.getId());
            draftNodeEntity.setProcDefKey(draftEntity.getProcDefKey());
            if (!StringUtils.isBlank(nodeDto.getRoutineExpression())) {
                draftNodeEntity.setRoutineExp(nodeDto.getRoutineExpression());
            }
            if (!StringUtils.isBlank(nodeDto.getRoutineRaw())) {
                draftNodeEntity.setRoutineRaw(nodeDto.getRoutineRaw());
            }

            if (!StringUtils.isBlank(nodeDto.getServiceId())) {
                draftNodeEntity.setServiceId(nodeDto.getServiceId());
            }
            draftNodeEntity.setServiceName(nodeDto.getServiceName());
            draftNodeEntity.setTimeoutExp(nodeDto.getTimeoutExpression());
            draftNodeEntity.setUpdatedTime(currTime);
            draftNodeEntity.setUpdatedBy(currUser);
            draftNodeEntity.setTaskCategory(nodeDto.getTaskCategory());
            draftNodeEntity.setPreCheck(nodeDto.getPreCheck());
            draftNodeEntity.setDynamicBind(nodeDto.getDynamicBind());
            draftNodeEntity.setPrevCtxNodeIds(nodeDto.getPrevCtxNodeIds());

            taskNodeDefInfoRepo.updateByPrimaryKeySelective(draftNodeEntity);

            processDraftParamInfos(nodeDto, draftEntity, draftNodeEntity, currTime);

            TaskNodeDefInfoDto nodeDtoResult = new TaskNodeDefInfoDto();
            nodeDtoResult.setNodeDefId(draftNodeEntity.getId());
            nodeDtoResult.setNodeId(draftNodeEntity.getNodeId());
            nodeDtoResult.setNodeName(draftNodeEntity.getNodeName());
            nodeDtoResult.setStatus(draftNodeEntity.getStatus());

            procDefResult.addTaskNodeInfo(nodeDtoResult);

        }

        if (startEventNodeDto == null) {
            ProcFlowNode startProcFlowNode = tryFindoutStartEventNode(procDefOutline);
            if (startProcFlowNode != null) {
                TaskNodeDefInfoEntity draftNodeEntity = taskNodeDefInfoRepo.selectOneWithProcessIdAndNodeIdAndStatus(
                        draftEntity.getId(), startProcFlowNode.getId(), TaskNodeDefInfoEntity.DRAFT_STATUS);

                if (draftNodeEntity == null) {
                    draftNodeEntity = new TaskNodeDefInfoEntity();
                    draftNodeEntity.setId(LocalIdGenerator.generateId());
                    draftNodeEntity.setStatus(TaskNodeDefInfoEntity.DRAFT_STATUS);
                    draftNodeEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                    draftNodeEntity.setCreatedTime(currTime);

                    taskNodeDefInfoRepo.insert(draftNodeEntity);
                }

                draftNodeEntity.setNodeId(startProcFlowNode.getId());
                draftNodeEntity.setNodeName(startProcFlowNode.getNodeName());
                draftNodeEntity.setNodeType(startProcFlowNode.getNodeType());
                draftNodeEntity.setProcDefId(draftEntity.getId());
                draftNodeEntity.setProcDefKey(draftEntity.getProcDefKey());

                draftNodeEntity.setUpdatedTime(currTime);
                draftNodeEntity.setUpdatedBy(currUser);

                taskNodeDefInfoRepo.updateByPrimaryKeySelective(draftNodeEntity);

                TaskNodeDefInfoDto nodeDtoResult = new TaskNodeDefInfoDto();
                nodeDtoResult.setNodeDefId(draftNodeEntity.getId());
                nodeDtoResult.setNodeId(draftNodeEntity.getNodeId());
                nodeDtoResult.setNodeName(draftNodeEntity.getNodeName());
                nodeDtoResult.setStatus(draftNodeEntity.getStatus());

                procDefResult.addTaskNodeInfo(nodeDtoResult);
            }
        }
    }
    
    private void processDraftParamInfos(TaskNodeDefInfoDto nodeDto, ProcDefInfoEntity draftEntity,
            TaskNodeDefInfoEntity draftNodeEntity, Date currTime) {
        if (nodeDto.getParamInfos() == null || nodeDto.getParamInfos().isEmpty()) {
            tryClearAllParamInfos(draftEntity, draftNodeEntity);
            return;
        }

        List<TaskNodeParamEntity> reusedDraftParamEntities = new ArrayList<>();
        for (TaskNodeDefParamDto nodeParamDto : nodeDto.getParamInfos()) {
            String nodeParamOid = nodeParamDto.getId();
            TaskNodeParamEntity draftNodeParamEntity = null;
            if (!StringUtils.isBlank(nodeParamOid)) {
                TaskNodeParamEntity npEntity = taskNodeParamRepo.selectByPrimaryKey(nodeParamOid);
                if (npEntity != null) {
                    if (TaskNodeParamEntity.DRAFT_STATUS.equals(npEntity.getStatus())) {
                        draftNodeParamEntity = npEntity;
                    }
                }
            }

            if (draftNodeParamEntity == null) {
                draftNodeParamEntity = new TaskNodeParamEntity();
                draftNodeParamEntity.setId(LocalIdGenerator.generateId());
                draftNodeParamEntity.setStatus(TaskNodeParamEntity.DRAFT_STATUS);
                draftNodeParamEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                draftNodeParamEntity.setCreatedTime(currTime);
                taskNodeParamRepo.insert(draftNodeParamEntity);
            }

            draftNodeParamEntity.setNodeId(
                    StringUtils.isBlank(nodeParamDto.getNodeId()) ? nodeDto.getNodeId() : nodeParamDto.getNodeId());
            draftNodeParamEntity.setBindNodeId(nodeParamDto.getBindNodeId());
            draftNodeParamEntity.setBindParamName(nodeParamDto.getBindParamName());
            draftNodeParamEntity.setBindParamType(nodeParamDto.getBindParamType());
            draftNodeParamEntity.setParamName(nodeParamDto.getParamName());
            draftNodeParamEntity.setProcDefId(draftEntity.getId());
            draftNodeParamEntity.setTaskNodeDefId(draftNodeEntity.getId());
            draftNodeParamEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            draftNodeParamEntity.setUpdatedTime(currTime);
            draftNodeParamEntity.setBindType(nodeParamDto.getBindType());
            draftNodeParamEntity.setBindVal(nodeParamDto.getBindValue());

            taskNodeParamRepo.updateByPrimaryKeySelective(draftNodeParamEntity);

            reusedDraftParamEntities.add(draftNodeParamEntity);

        }

        tryClearAbandonedParamInfos(draftEntity, draftNodeEntity, reusedDraftParamEntities);
    }
    
    private void tryClearAllParamInfos(ProcDefInfoEntity draftEntity, TaskNodeDefInfoEntity draftNodeEntity) {
        List<TaskNodeParamEntity> existParamEntities = taskNodeParamRepo
                .selectAllDraftByProcDefIdAndTaskNodeDefId(draftEntity.getId(), draftNodeEntity.getId());
        if (existParamEntities == null || existParamEntities.isEmpty()) {
            return;
        }
        for (TaskNodeParamEntity paramEntity : existParamEntities) {
            taskNodeParamRepo.deleteByPrimaryKey(paramEntity.getId());
        }
    }

    private void tryClearAbandonedParamInfos(ProcDefInfoEntity draftEntity, TaskNodeDefInfoEntity draftNodeEntity,
            List<TaskNodeParamEntity> reusedDraftParamEntities) {
        List<TaskNodeParamEntity> dbParamEntities = taskNodeParamRepo
                .selectAllDraftByProcDefIdAndTaskNodeDefId(draftEntity.getId(), draftNodeEntity.getId());
        for (TaskNodeParamEntity dbParamEntity : dbParamEntities) {
            TaskNodeParamEntity reusedEntity = findTaskNodeParamEntityFromListById(reusedDraftParamEntities,
                    dbParamEntity.getId());
            if (reusedEntity != null) {
                continue;
            }

            taskNodeParamRepo.deleteByPrimaryKey(dbParamEntity.getId());
        }
    }
    
    private TaskNodeDefInfoEntity tryFindDraftNodeEntity(String nodeOid, String procDefId, String nodeId) {
        TaskNodeDefInfoEntity draftNodeEntity = null;
        if (!StringUtils.isBlank(nodeOid)) {
            TaskNodeDefInfoEntity nEntity = taskNodeDefInfoRepo.selectByPrimaryKey(nodeOid);
            if (nEntity != null) {
                if (TaskNodeDefInfoEntity.DRAFT_STATUS.equals(nEntity.getStatus())) {
                    draftNodeEntity = nEntity;
                }
            }
        } else {
            List<TaskNodeDefInfoEntity> nEntities = taskNodeDefInfoRepo.selectAllByProcDefIdAndNodeId(procDefId,
                    nodeId);
            if (nEntities == null || nEntities.isEmpty()) {
                return null;
            }

            return nEntities.get(0);
        }

        return draftNodeEntity;
    }

    private void tryClearOutDatedDraftNodeEntity(TaskNodeDefInfoEntity draftNodeEntity) {
        if (draftNodeEntity == null) {
            return;
        }

        List<TaskNodeParamEntity> taskNodeParamEntities = taskNodeParamRepo
                .selectAllByProcDefIdAndTaskNodeDefId(draftNodeEntity.getProcDefId(), draftNodeEntity.getId());
        for (TaskNodeParamEntity np : taskNodeParamEntities) {
            taskNodeParamRepo.deleteByPrimaryKey(np.getId());
        }

        taskNodeDefInfoRepo.deleteByPrimaryKey(draftNodeEntity.getId());
    }
    
    private void validateTaskInfos(ProcDefInfoDto procDefInfoDto) {
        if (procDefInfoDto.getTaskNodeInfos() == null) {
            return;
        }

        Map<String, List<String>> permissionToRole = procDefInfoDto.getPermissionToRole();
        if (permissionToRole == null || permissionToRole.isEmpty()) {
            log.warn("Permission configurations not found for {}", procDefInfoDto.getProcDefName());
            throw new WecubeCoreException("3213", "Permission configuration should provide.");
        }

        List<String> mgmtRoleNames = permissionToRole.get(PluginConfigRoles.PERM_TYPE_MGMT);
        if (mgmtRoleNames == null || mgmtRoleNames.isEmpty()) {
            log.warn("Management permission configuration not found for {}", procDefInfoDto.getProcDefName());
            throw new WecubeCoreException("3214", "Management permission configuration should provide.");
        }

        for (TaskNodeDefInfoDto nodeDto : procDefInfoDto.getTaskNodeInfos()) {
            if (!"subProcess".equalsIgnoreCase(nodeDto.getNodeType())) {
                continue;
            }
            if (StringUtils.isBlank(nodeDto.getRoutineExpression())) {
                throw new WecubeCoreException("3215",
                        String.format("Routine expression is blank for %s", nodeDto.getNodeId()), nodeDto.getNodeId());
            }

            if (StringUtils.isBlank(nodeDto.getServiceId())) {
                throw new WecubeCoreException("3216", String.format("Service ID is blank for %s", nodeDto.getNodeId()),
                        nodeDto.getNodeId());
            }

            validateTaskNodePluginPermission(nodeDto, mgmtRoleNames);
        }
    }
    
    private void validateTaskNodePluginPermission(TaskNodeDefInfoDto nodeDto, List<String> mgmtRoleNames) {
        PluginConfigInterfaces intf = retrievePluginConfigInterface(nodeDto, nodeDto.getNodeId());
        PluginConfigs pluginConfig = intf.getPluginConfig();
        if (pluginConfig == null) {
            log.warn("Plugin config does not exist for {} {}", nodeDto.getServiceId(), intf.getId());
            throw new WecubeCoreException("3217",
                    String.format("Plugin config does not exist for interface: %s", nodeDto.getServiceId()),
                    nodeDto.getServiceId());
        }

        List<PluginConfigRoles> pluginAuthConfigEntities = this.pluginAuthRepository
                .selectAllByPluginConfigAndPerm(pluginConfig.getId(), PluginConfigRoles.PERM_TYPE_USE);
        if (pluginAuthConfigEntities == null || pluginAuthConfigEntities.isEmpty()) {
            log.error("Plugin permission configuration does not exist for {}", pluginConfig.getId());
            throw new WecubeCoreException("3218", "Lack of plugin permission to deploy workflow definition.");
        }

        for (PluginConfigRoles pluginAuthConfigEntity : pluginAuthConfigEntities) {
            if (CollectionUtils.collectionContains(mgmtRoleNames, pluginAuthConfigEntity.getRoleName())) {
                return;
            }
        }

        log.warn("Lack of permission to deploy process,managementRoles={},pluginConfigId={}", mgmtRoleNames,
                pluginConfig.getId());
        throw new WecubeCoreException("3219", "Lack of permission to deploy process.");
    }
    
    private PluginConfigInterfaces retrievePluginConfigInterface(TaskNodeDefInfoDto taskNodeDefDto, String nodeId) {

        String serviceId = taskNodeDefDto.getServiceId();
        if (StringUtils.isBlank(serviceId)) {
            log.error("service ID is invalid for {} {}", taskNodeDefDto.getProcDefId(), nodeId);
            throw new WecubeCoreException("3211", "Service ID is invalid.");
        }

        if (log.isDebugEnabled()) {
            log.debug("retrieved service id {} for {},{}", serviceId, taskNodeDefDto.getProcDefId(), nodeId);
        }

        PluginConfigInterfaces pluginConfigInterface = pluginConfigMgmtService
                .getPluginConfigInterfaceByServiceName(serviceId);

        if (pluginConfigInterface == null) {
            log.error("Plugin config interface does not exist for {} {} {}", taskNodeDefDto.getNodeId(), nodeId,
                    serviceId);
            throw new WecubeCoreException("3212", "Plugin config interface does not exist.");
        }

        return pluginConfigInterface;
    }
    
    private ProcessDeploymentResultDto tryPerformNewProcessDeployment(ProcDefInfoDto procDefInfoDto) {
        String originalId = procDefInfoDto.getProcDefId();

        Date currTime = new Date();

        ProcDefInfoEntity draftProcDefEntity = null;
        if (!StringUtils.isBlank(originalId)) {
            ProcDefInfoEntity entity = processDefInfoRepo.selectByPrimaryKey(originalId);
            if (entity != null && ProcDefInfoEntity.DRAFT_STATUS.equals(entity.getStatus())) {
                draftProcDefEntity = entity;
            }
        }

        ProcDefInfoEntity procDefEntity = new ProcDefInfoEntity();
        procDefEntity.setId(LocalIdGenerator.generateId());
        procDefEntity.setProcDefData(procDefInfoDto.getProcDefData());
        procDefEntity.setProcDefKey(procDefInfoDto.getProcDefKey());
        procDefEntity.setRootEntity(procDefInfoDto.getRootEntity());
        procDefEntity.setStatus(ProcDefInfoEntity.PREDEPLOY_STATUS);
        procDefEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        procDefEntity.setCreatedTime(currTime);
        procDefEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        procDefEntity.setUpdatedTime(currTime);
        procDefEntity.setExcludeMode(procDefInfoDto.getExcludeMode());
        procDefEntity.setTags(procDefInfoDto.getTags());

        processDefInfoRepo.insert(procDefEntity);
        // Save ProcRoleBindingEntity
        this.saveProcRoleBinding(procDefEntity.getId(), procDefInfoDto);

        processDeployTaskNodeInfos(procDefInfoDto, procDefEntity, currTime);

        ProcessDefinition procDef = null;
        boolean deployFailed = false;
        try {
            procDef = workflowEngineService.deployProcessDefinition(procDefInfoDto);
        } catch (BpmnCustomizationException e) {
            log.warn("failed to deploy process definition,msg={}", e.getMessage());
            deployFailed = true;
            handleDeployFailure(procDefEntity);
        }

        if (deployFailed || procDef == null) {
            throw new WecubeCoreException("3210", "Failed to deploy process definition.");
        }

        if (draftProcDefEntity != null) {
            purgeProcessDefInfoEntity(draftProcDefEntity);
        }

        ProcDefOutline procDefOutline = workflowEngineService.getProcDefOutline(procDef);

        ProcDefOutlineDto outlineDto = postDeployProcessDefinition(procDefEntity, procDef, procDefOutline);

        ProcessDeploymentResultDto processDeploymentResultDto = new ProcessDeploymentResultDto();
        processDeploymentResultDto.setResult(outlineDto);
        processDeploymentResultDto.setStatus(ProcessDeploymentResultDto.STATUS_OK);

        return processDeploymentResultDto;
    }
    
    private void processDeployTaskNodeInfos(ProcDefInfoDto procDefInfoDto, ProcDefInfoEntity procDefEntity,
            Date currTime) {
        if (procDefInfoDto.getTaskNodeInfos() != null) {
            for (TaskNodeDefInfoDto nodeDto : procDefInfoDto.getTaskNodeInfos()) {
                TaskNodeDefInfoEntity nodeEntity = buildDeployNewTaskNodeDefInfoEntity(nodeDto, procDefEntity,
                        currTime);
                taskNodeDefInfoRepo.insert(nodeEntity);

                if (nodeDto.getParamInfos() != null) {
                    for (TaskNodeDefParamDto paramDto : nodeDto.getParamInfos()) {
                        TaskNodeParamEntity paramEntity = buildDeployNewTaskNodeParamEntity(paramDto, nodeDto,
                                procDefEntity, nodeEntity, currTime);

                        taskNodeParamRepo.insert(paramEntity);
                    }
                }
            }
        }
    }

    private ProcDefOutlineDto postDeployProcessDefinition(ProcDefInfoEntity procDefEntity, ProcessDefinition procDef,
            ProcDefOutline procDefOutline) {
        if (procDefEntity == null) {
            return null;
        }

        Date now = new Date();
        procDefEntity.setProcDefKernelId(procDef.getId());
        procDefEntity.setProcDefKey(procDef.getKey());
        procDefEntity.setProcDefName(procDef.getName());
        procDefEntity.setProcDefVer(procDef.getVersion());
        procDefEntity.setStatus(ProcDefInfoEntity.DEPLOYED_STATUS);
        procDefEntity.setUpdatedTime(now);
        procDefEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());

        processDefInfoRepo.updateByPrimaryKeySelective(procDefEntity);

        ProcDefOutlineDto result = new ProcDefOutlineDto();
        result.setProcDefId(procDefEntity.getId());
        result.setProcDefKey(procDefEntity.getProcDefKey());
        result.setProcDefName(procDefEntity.getProcDefName());
        result.setProcDefVersion(String.valueOf(procDef.getVersion()));
        result.setRootEntity(procDefEntity.getRootEntity());
        result.setStatus(procDefEntity.getStatus());
        result.setExcludeMode(procDefEntity.getExcludeMode());
        result.setTags(procDefEntity.getTags());

        List<TaskNodeParamEntity> nodeParamEntities = taskNodeParamRepo.selectAllByProcDefId(procDefEntity.getId());
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.selectAllByProcDefId(procDefEntity.getId());

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
                nodeEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                nodeEntity.setCreatedTime(now);
                taskNodeDefInfoRepo.insert(nodeEntity);
            } else {
                nodeEntity.setUpdatedTime(now);
                nodeEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            }
            nodeEntity.setNodeName(pfn.getNodeName());
            nodeEntity.setNodeType(pfn.getNodeType());
            nodeEntity.setStatus(TaskNodeDefInfoEntity.DEPLOYED_STATUS);
            nodeEntity.setProcDefKernelId(procDef.getId());
            nodeEntity.setProcDefKey(procDef.getKey());
            nodeEntity.setProcDefVer(procDef.getVersion());
            if (NODE_SUB_PROCESS.equals(pfn.getNodeType()) || "serviceTask".equals(pfn.getNodeType())) {
                nodeEntity.setOrderedNo(String.valueOf(orderedNo.getAndIncrement()));
            }
            nodeEntity.setPrevNodeIds(marshalNodeIds(pfn.getPreviousFlowNodes()));
            nodeEntity.setSucceedNodeIds(marshalNodeIds(pfn.getSucceedingFlowNodes()));
            nodeEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            nodeEntity.setUpdatedTime(now);
            taskNodeDefInfoRepo.updateByPrimaryKeySelective(nodeEntity);

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
                n.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                n.setStatus(TaskNodeParamEntity.DEPLOYED_STATUS);
                taskNodeParamRepo.updateByPrimaryKeySelective(n);
            });
        }

        List<TaskNodeParamEntity> nodeParamEntitiesToRemove = taskNodeParamRepo
                .selectAllByProcDefIdAndStatus(procDefEntity.getId(), TaskNodeParamEntity.PREDEPLOY_STATUS);
        List<TaskNodeDefInfoEntity> nodeEntitiesToRemove = taskNodeDefInfoRepo
                .selectAllByProcDefIdAndStatus(procDefEntity.getId(), TaskNodeParamEntity.PREDEPLOY_STATUS);

        nodeParamEntitiesToRemove.forEach(m -> {
            taskNodeParamRepo.deleteByPrimaryKey(m.getId());
        });

        nodeEntitiesToRemove.forEach(m -> {
            taskNodeDefInfoRepo.deleteByPrimaryKey(m.getId());
        });

        return result;
    }
    
    private TaskNodeParamEntity buildDeployNewTaskNodeParamEntity(TaskNodeDefParamDto paramDto,
            TaskNodeDefInfoDto nodeDto, ProcDefInfoEntity procDefEntity, TaskNodeDefInfoEntity nodeEntity,
            Date currTime) {
        TaskNodeParamEntity paramEntity = new TaskNodeParamEntity();
        paramEntity.setId(LocalIdGenerator.generateId());
        paramEntity.setNodeId(StringUtils.isBlank(paramDto.getNodeId()) ? nodeDto.getNodeId() : paramDto.getNodeId());
        paramEntity.setBindNodeId(paramDto.getBindNodeId());
        paramEntity.setBindParamName(paramDto.getBindParamName());
        paramEntity.setBindParamType(paramDto.getBindParamType());
        paramEntity.setParamName(paramDto.getParamName());
        paramEntity.setProcDefId(procDefEntity.getId());
        paramEntity.setStatus(TaskNodeParamEntity.PREDEPLOY_STATUS);
        paramEntity.setTaskNodeDefId(nodeEntity.getId());
        paramEntity.setUpdatedTime(currTime);
        paramEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        paramEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        paramEntity.setCreatedTime(currTime);
        paramEntity.setBindType(paramDto.getBindType());
        paramEntity.setBindVal(paramDto.getBindValue());

        return paramEntity;
    }

    

    

    private void handleDeployFailure(ProcDefInfoEntity procEntity) {
        if (procEntity == null) {
            return;
        }
        purgeProcessDefInfoEntity(procEntity);
    }
    
    private void purgeProcessDefInfoEntity(ProcDefInfoEntity procEntity) {
        List<TaskNodeParamEntity> nodeParamEntities = taskNodeParamRepo.selectAllByProcDefId(procEntity.getId());
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.selectAllByProcDefId(procEntity.getId());

        if (nodeParamEntities != null && !nodeParamEntities.isEmpty()) {
            for (TaskNodeParamEntity nodeParamEntity : nodeParamEntities) {
                taskNodeParamRepo.deleteByPrimaryKey(nodeParamEntity.getId());
            }
        }

        if (nodeEntities != null && !nodeEntities.isEmpty()) {
            for (TaskNodeDefInfoEntity nodeEntity : nodeEntities) {
                taskNodeDefInfoRepo.deleteByPrimaryKey(nodeEntity.getId());
            }
        }

        processDefInfoRepo.deleteByPrimaryKey(procEntity.getId());
    }
    
    private ProcessDeploymentResultDto tryPerformExistingProcessDeployment(ProcDefInfoEntity existingProcDef,
            ProcDefInfoDto procDefInfoDto, String continueToken) {
        // #2222
        log.info("such process definition name already exists,procDefName={}", procDefInfoDto.getProcDefName());
        if (StringUtils.isNoneBlank(continueToken)) {
            return tryPerformConditionalProcessDeploymentEdition(existingProcDef, procDefInfoDto, continueToken);
        }

        if (!verifyConditionalProcessDeploymentEdition(existingProcDef, procDefInfoDto)) {
            log.error("Process definition name should NOT duplicated.procDefName={}", procDefInfoDto.getProcDefName());
            throw new WecubeCoreException("3209", "Process definition name should NOT duplicated.");
        }

        String newContinueToken = buildProcessDeploymentContinueToken(procDefInfoDto);
        String message = "Such process already had been deployed before,please confirm to proceed deployment.";
        ProcessDeploymentResultDto resultDto = new ProcessDeploymentResultDto();
        resultDto.setStatus(ProcessDeploymentResultDto.STATUS_CONFIRM);
        resultDto.setMessage(message);
        ContinueTokenInfoDto continueTokenInfo = new ContinueTokenInfoDto();
        continueTokenInfo.setContinueToken(newContinueToken);
        resultDto.setContinueToken(continueTokenInfo);

        return resultDto;
    }
    
    private boolean verifyConditionalProcessDeploymentEdition(ProcDefInfoEntity existingProcDef,
            ProcDefInfoDto procDefInfoDto) {
        if (!Objects.equals(existingProcDef.getId(), procDefInfoDto.getProcDefId())) {
            return false;
        }

        if (!Objects.equals(existingProcDef.getProcDefName(), procDefInfoDto.getProcDefName())) {
            return false;
        }

        List<TaskNodeDefInfoEntity> taskNodeDefEntities = taskNodeDefInfoRepo
                .selectAllByProcDefId(existingProcDef.getId());
        int existingNodeSize = (taskNodeDefEntities == null ? 0 : taskNodeDefEntities.size());
        int newNodeSize = procDefInfoDto.getTaskNodeInfos() == null ? 0 : procDefInfoDto.getTaskNodeInfos().size();
        if (newNodeSize != existingNodeSize) {
            return false;
        }

        return verifyConditionalProcessDeploymentEdition(taskNodeDefEntities, procDefInfoDto.getTaskNodeInfos());
    }
    
    private boolean verifyConditionalProcessDeploymentEdition(List<TaskNodeDefInfoEntity> taskNodeDefEntities,
            List<TaskNodeDefInfoDto> taskNodeInfos) {
        if (taskNodeDefEntities == null || taskNodeInfos == null) {
            return false;
        }

        if (taskNodeDefEntities.size() != taskNodeInfos.size()) {
            return false;
        }

        Map<String, TaskNodeDefInfoDto> idAndTaskNodeDefInfoDtos = new HashMap<>();
        for (TaskNodeDefInfoDto nodeInfoDto : taskNodeInfos) {
            if (StringUtils.isBlank(nodeInfoDto.getNodeId()) || StringUtils.isBlank(nodeInfoDto.getNodeDefId())) {
                return false;
            }
            idAndTaskNodeDefInfoDtos.put(nodeInfoDto.getNodeId(), nodeInfoDto);
        }

        for (TaskNodeDefInfoEntity e : taskNodeDefEntities) {
            TaskNodeDefInfoDto nodeInfoDto = idAndTaskNodeDefInfoDtos.get(e.getNodeId());

            if (nodeInfoDto == null) {
                return false;
            }

            if (!verifyConditionalProcessDeploymentEdition(e, nodeInfoDto)) {
                return false;
            }
        }
        return true;
    }

    private boolean verifyConditionalProcessDeploymentEdition(TaskNodeDefInfoEntity taskNodeDefEntity,
            TaskNodeDefInfoDto taskNodeInfo) {
        if (!Objects.equals(taskNodeDefEntity.getId(), taskNodeInfo.getNodeDefId())) {
            return false;
        }

        if (!Objects.equals(taskNodeDefEntity.getNodeId(), taskNodeInfo.getNodeId())) {
            return false;
        }

        if (!Objects.equals(taskNodeDefEntity.getRoutineExp(), taskNodeInfo.getRoutineExpression())) {
            return false;
        }

        if (!Objects.equals(taskNodeDefEntity.getServiceId(), taskNodeInfo.getServiceId())) {
            return false;
        }

        return true;
    }

    private ProcessDeploymentResultDto tryPerformConditionalProcessDeploymentEdition(ProcDefInfoEntity existingProcDef,
            ProcDefInfoDto procDefInfoDto, String continueToken) {
        if (!verifyProcessDeploymentContinueToken(procDefInfoDto, continueToken)) {
            throw new WecubeCoreException("Invalid continue token provided.");
        }

        List<TaskNodeDefInfoDto> taskNodeInfos = procDefInfoDto.getTaskNodeInfos();
        if (taskNodeInfos == null || taskNodeInfos.isEmpty()) {
            throw new WecubeCoreException("Invalid task nodes provided.");
        }

        Map<String, TaskNodeDefInfoDto> idAndTaskNodeDefInfoDtos = new HashMap<>();
        for (TaskNodeDefInfoDto nodeDefInfoDto : taskNodeInfos) {
            idAndTaskNodeDefInfoDtos.put(nodeDefInfoDto.getNodeDefId(), nodeDefInfoDto);
        }

        List<TaskNodeDefInfoEntity> taskNodeDefEntities = taskNodeDefInfoRepo
                .selectAllByProcDefId(existingProcDef.getId());

        for (TaskNodeDefInfoEntity nodeInfoEntity : taskNodeDefEntities) {
            TaskNodeDefInfoDto nodeDefInfoDto = idAndTaskNodeDefInfoDtos.get(nodeInfoEntity.getId());
            if (nodeDefInfoDto == null) {
                continue;
            }

            tryPerformConditionalProcessDeploymentEdition(nodeInfoEntity, nodeDefInfoDto);
        }

        ProcessDeploymentResultDto resultDto = new ProcessDeploymentResultDto();
        resultDto.setStatus(ProcessDeploymentResultDto.STATUS_OK);
        resultDto.setMessage("success");

        ProcDefOutlineDto outlineDto = getProcessDefinitionOutline(existingProcDef.getId());
        resultDto.setResult(outlineDto);
        return resultDto;
    }

    private void tryPerformConditionalProcessDeploymentEdition(TaskNodeDefInfoEntity nodeInfoEntity,
            TaskNodeDefInfoDto nodeDefInfoDto) {
        List<TaskNodeDefParamDto> paramInfoDtos = nodeDefInfoDto.getParamInfos();
        if (paramInfoDtos == null || paramInfoDtos.isEmpty()) {
            return;
        }

        for (TaskNodeDefParamDto paramDto : paramInfoDtos) {
            if (StringUtils.isBlank(paramDto.getParamName())) {
                continue;
            }

            if (StringUtils.isBlank(paramDto.getNodeId())) {
                continue;
            }

            TaskNodeParamEntity paramEntity = taskNodeParamRepo
                    .selectOneByTaskNodeDefIdAndParamName(nodeInfoEntity.getId(), paramDto.getParamName());

            if (paramEntity == null) {
                paramEntity = new TaskNodeParamEntity();
                paramEntity.setId(LocalIdGenerator.generateId());
                paramEntity.setNodeId(paramDto.getNodeId());
                paramEntity.setBindNodeId(paramDto.getBindNodeId());
                paramEntity.setBindParamName(paramDto.getBindParamName());
                paramEntity.setBindParamType(paramDto.getBindParamType());
                paramEntity.setParamName(paramDto.getParamName());
                paramEntity.setProcDefId(nodeInfoEntity.getProcDefId());
                paramEntity.setStatus(TaskNodeParamEntity.DEPLOYED_STATUS);
                paramEntity.setTaskNodeDefId(nodeInfoEntity.getId());
                paramEntity.setUpdatedTime(new Date());
                paramEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                paramEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
                paramEntity.setCreatedTime(new Date());
                paramEntity.setBindType(paramDto.getBindType());
                paramEntity.setBindVal(paramDto.getBindValue());

                taskNodeParamRepo.insert(paramEntity);
            } else {
                paramEntity.setBindNodeId(paramDto.getBindNodeId());
                paramEntity.setBindParamName(paramDto.getBindParamName());
                paramEntity.setBindParamType(paramDto.getBindParamType());
                paramEntity.setUpdatedTime(new Date());
                paramEntity.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
                paramEntity.setBindType(paramDto.getBindType());
                paramEntity.setBindVal(paramDto.getBindValue());

                taskNodeParamRepo.updateByPrimaryKeySelective(paramEntity);
            }
        }
    }
    
    private String buildProcessDeploymentContinueToken(ProcDefInfoDto procDefInfoDto) {
        StringBuilder data = new StringBuilder();
        String seperator = ":";
        data.append(procDefInfoDto.getClass().getName()).append(seperator);
        data.append(procDefInfoDto.getProcDefKey()).append(seperator);
        data.append(procDefInfoDto.getProcDefName()).append(seperator);

        String md5 = DigestUtils.md5DigestAsHex(data.toString().getBytes(Charset.forName("UTF-8")));
        return md5;
    }

    private boolean verifyProcessDeploymentContinueToken(ProcDefInfoDto procDefInfoDto, String inputContinueToken) {
        String genContinueToken = buildProcessDeploymentContinueToken(procDefInfoDto);

        return genContinueToken.equals(inputContinueToken);
    }

}
