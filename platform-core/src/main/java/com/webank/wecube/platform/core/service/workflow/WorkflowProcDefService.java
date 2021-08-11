package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;

/**
 * 
 * 
 *
 */
@Service
public class WorkflowProcDefService extends AbstractWorkflowProcDefService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcDefService.class);

    /**
     * 
     * @param procDefId
     */
    public void removeProcessDefinition(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            throw new WecubeCoreException("3205", "Process definition id is blank.");
        }

        ProcDefInfoEntity procDef = processDefInfoRepo.selectByPrimaryKey(procDefId);

        if (procDef == null) {
            log.warn("such process definition does not exist:{}", procDefId);
            return;
        }

        this.processRoleService.checkPermission(procDef.getId(), ProcRoleBindingEntity.MGMT);

        if (!ProcDefInfoEntity.DRAFT_STATUS.equals(procDef.getStatus())) {
            // set NOT DRAFT_STATUS process to DELETED_STATUS, without deleting
            // the nodes and params
            log.info(String.format("Setting process: [%s]'s status to deleted status: [%s]", procDefId,
                    ProcDefInfoEntity.DELETED_STATUS));
            procDef.setStatus(ProcDefInfoEntity.DELETED_STATUS);
            procDef.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
            procDef.setUpdatedTime(new Date());
            processDefInfoRepo.updateByPrimaryKeySelective(procDef);
            return;
        }
        // delete DRAFT_STATUS process with all nodes and params deleted as well
        List<TaskNodeParamEntity> nodeParams = taskNodeParamRepo.selectAllByProcDefId(procDef.getId());

        if (nodeParams != null) {
            for (TaskNodeParamEntity p : nodeParams) {
                taskNodeParamRepo.deleteByPrimaryKey(p.getId());
            }
        }

        List<TaskNodeDefInfoEntity> nodeDefs = taskNodeDefInfoRepo.selectAllByProcDefId(procDef.getId());
        if (nodeDefs != null) {
            for (TaskNodeDefInfoEntity n : nodeDefs) {
                taskNodeDefInfoRepo.deleteByPrimaryKey(n.getId());
            }
        }

        if (log.isInfoEnabled()) {
            log.info("process definition with id {} had been deleted successfully.", procDefId);
        }

        processDefInfoRepo.deleteByPrimaryKey(procDef.getId());
    }

    /**
     * 
     * @param procDefId
     * @param taskNodeId
     * @param prevCtxNodeIds
     * @return
     */
    public List<TaskNodeDefBriefDto> getRootContextTaskNodes(String procDefId, String taskNodeId,
            String prevCtxNodeIds) {
        List<TaskNodeDefBriefDto> result = new ArrayList<>();
        if (StringUtils.isBlank(procDefId)) {
            return result;
        }
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.selectAllByProcDefId(procDefId);
        if (nodeEntities == null || nodeEntities.isEmpty()) {
            return result;
        }

        List<TaskNodeDefInfoEntity> filteredNodeEntities = new ArrayList<>();
        if (StringUtils.isBlank(prevCtxNodeIds)) {
            nodeEntities.forEach(e -> {
                if (TaskNodeDefInfoEntity.NODE_TYPE_START_EVENT.equalsIgnoreCase(e.getNodeType())
                        || StringUtils.isBlank(e.getNodeType())) {
                    filteredNodeEntities.add(e);
                }
            });
        } else {
            filterTaskNodeInfosByRootContext(prevCtxNodeIds, filteredNodeEntities, nodeEntities);
        }

        // #1993
        filteredNodeEntities.forEach(e -> {
            if (TaskNodeDefInfoEntity.NODE_TYPE_SUBPROCESS.equalsIgnoreCase(e.getNodeType())
                    || TaskNodeDefInfoEntity.NODE_TYPE_SERVICE_TASK.equalsIgnoreCase(e.getNodeType())
                    || TaskNodeDefInfoEntity.NODE_TYPE_START_EVENT.equalsIgnoreCase(e.getNodeType())
                    || StringUtils.isBlank(e.getNodeType())) {
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

    private void filterTaskNodeInfosByRootContext(String prevCtxNodeIds,
            List<TaskNodeDefInfoEntity> filteredNodeEntities, List<TaskNodeDefInfoEntity> nodeEntities) {
        String[] prevCtxNodeIdsParts = prevCtxNodeIds.trim().split(",");
        for (String prevCtxNodeIdsPart : prevCtxNodeIdsParts) {
            TaskNodeDefInfoEntity nodeDefInfo = pickoutByNodeId(prevCtxNodeIdsPart, filteredNodeEntities);
            if (nodeDefInfo == null) {
                nodeDefInfo = pickoutByNodeId(prevCtxNodeIdsPart, nodeEntities);
                if (nodeDefInfo != null) {
                    filteredNodeEntities.add(nodeDefInfo);
                }
            } else {
                // means exist already
            }

            if (nodeDefInfo != null) {
                String supPrevCtxNodeIds = nodeDefInfo.getPrevCtxNodeIds();
                if (StringUtils.isNoneBlank(supPrevCtxNodeIds)) {
                    filterTaskNodeInfosByRootContext(supPrevCtxNodeIds, filteredNodeEntities, nodeEntities);
                }
            }
        }
    }

    private TaskNodeDefInfoEntity pickoutByNodeId(String nodeId, List<TaskNodeDefInfoEntity> nodeEntities) {
        if (nodeEntities == null || nodeEntities.isEmpty()) {
            return null;
        }

        for (TaskNodeDefInfoEntity n : nodeEntities) {
            if (nodeId.equals(n.getNodeId())) {
                return n;
            }
        }

        return null;
    }

    /**
     * 
     * @param procDefId
     * @return
     */
    public List<TaskNodeDefBriefDto> getTaskNodeBriefs(String procDefId) {
        List<TaskNodeDefBriefDto> result = new ArrayList<>();
        List<TaskNodeDefInfoEntity> nodeEntities = taskNodeDefInfoRepo.selectAllByProcDefId(procDefId);
        if (nodeEntities == null || nodeEntities.isEmpty()) {
            return result;
        }

        // #1993
        nodeEntities.forEach(e -> {
            if (TaskNodeDefInfoEntity.NODE_TYPE_SUBPROCESS.equalsIgnoreCase(e.getNodeType())
                    || TaskNodeDefInfoEntity.NODE_TYPE_SERVICE_TASK.equalsIgnoreCase(e.getNodeType())
                    || TaskNodeDefInfoEntity.NODE_TYPE_START_EVENT.equalsIgnoreCase(e.getNodeType())
                    || StringUtils.isBlank(e.getNodeType())) {
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

    /**
     * 
     * @param id
     * @return
     */
    public ProcDefInfoDto getProcessDefinition(String id) {
        if (StringUtils.isBlank(id)) {
            throw new WecubeCoreException("3207", "Invalid process definition id");
        }
        ProcDefInfoDto result = doGetProcessDefinition(id);

        return result;
    }

    /**
     * 
     * @param includeDraftProcDef
     * @param permission
     * @return
     */
    public List<ProcDefInfoDto> getProcessDefinitions(boolean includeDraftProcDef, String permission, String tags) {
        List<String> currentUserRoleNameList = new ArrayList<>(
                Objects.requireNonNull(AuthenticationContextHolder.getCurrentUserRoles()));

        // check if there is permission specified
        List<ProcRoleDto> procRoleDtoList;
        if (!StringUtils.isBlank(permission)) {
            procRoleDtoList = processRoleService.retrieveProcessByRolesAndPermission(currentUserRoleNameList,
                    permission);
        } else {
            procRoleDtoList = processRoleService.retrieveAllProcessByRoles(currentUserRoleNameList);
        }
        Set<ProcRoleDto> procRoleDtoSet = new HashSet<>(procRoleDtoList);

        List<String> tagList = new ArrayList<String>();
        if (StringUtils.isNoneBlank(tags)) {
            String[] tagsParts = tags.trim().split(",");
            for (String tagsPart : tagsParts) {
                if (StringUtils.isNoneBlank(tagsPart)) {
                    tagList.add(tagsPart.trim());
                }
            }
        }

        // check if there is includeDraftProcDef specified
        List<ProcDefInfoEntity> procDefEntities = new ArrayList<>();
        for (ProcRoleDto procRoleDto : procRoleDtoSet) {
            String procId = procRoleDto.getProcessId();
            ProcDefInfoEntity processFoundById = processDefInfoRepo.selectByPrimaryKey(procId);
            if (processFoundById != null) {

                if (includeDraftProcDef) {
                    if (ProcDefInfoEntity.DEPLOYED_STATUS.equals(processFoundById.getStatus())
                            || ProcDefInfoEntity.DRAFT_STATUS.equals(processFoundById.getStatus())) {
                        if (!checkIfContains(procDefEntities, processFoundById)) {
                            procDefEntities.add(processFoundById);
                        }
                    }
                } else {
                    if (ProcDefInfoEntity.DEPLOYED_STATUS.equals(processFoundById.getStatus())) {
                        if (!checkIfContains(procDefEntities, processFoundById)) {
                            procDefEntities.add(processFoundById);
                        }
                    }
                }

            }
        }

        List<ProcDefInfoDto> procDefInfoDtos = new ArrayList<>();
        for (ProcDefInfoEntity e : procDefEntities) {
            if (!tagList.isEmpty()) {
                if (StringUtils.isNoneBlank(e.getTags())) {
                    for (String tag : tagList) {
                        if (tag.equalsIgnoreCase(e.getTags())) {
                            ProcDefInfoDto dto = procDefInfoDtoFromEntity(e);
                            procDefInfoDtos.add(dto);
                        }
                    }
                }
            } else {
                ProcDefInfoDto dto = procDefInfoDtoFromEntity(e);
                procDefInfoDtos.add(dto);
            }
        }

        Collections.sort(procDefInfoDtos, new Comparator<ProcDefInfoDto>() {

            @Override
            public int compare(ProcDefInfoDto o1, ProcDefInfoDto o2) {
                String o1Name = o1.getProcDefName();
                String o2Name = o2.getProcDefName();

                if (o1Name == null) {
                    return -1;
                }

                return o1Name.compareTo(o2Name);
            }

        });
        return procDefInfoDtos;
    }

    private boolean checkIfContains(List<ProcDefInfoEntity> procDefEntities, ProcDefInfoEntity processEntity) {
        for (ProcDefInfoEntity e : procDefEntities) {
            if (e.getId().equals(processEntity.getId())) {
                return true;
            }
        }

        return false;
    }

}
