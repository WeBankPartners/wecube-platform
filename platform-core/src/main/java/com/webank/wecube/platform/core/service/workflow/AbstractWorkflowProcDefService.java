package com.webank.wecube.platform.core.service.workflow;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefParamDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.repository.workflow.ProcDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeDefInfoMapper;
import com.webank.wecube.platform.core.repository.workflow.TaskNodeParamMapper;

public class AbstractWorkflowProcDefService extends AbstractWorkflowService{

    private static final Logger log = LoggerFactory.getLogger(AbstractWorkflowProcDefService.class);
    
    @Autowired
    protected ProcDefInfoMapper processDefInfoRepo;

    @Autowired
    protected TaskNodeDefInfoMapper taskNodeDefInfoRepo;

    @Autowired
    protected TaskNodeParamMapper taskNodeParamRepo;
    
    @Autowired
    protected ProcessRoleServiceImpl processRoleService;
    
    protected ProcDefInfoDto procDefInfoDtoFromEntity(ProcDefInfoEntity procDefEntity) {
        ProcDefInfoDto result = new ProcDefInfoDto();
        result.setProcDefId(procDefEntity.getId());
        result.setProcDefKey(procDefEntity.getProcDefKey());
        result.setProcDefName(procDefEntity.getProcDefName());
        result.setProcDefVersion(String.valueOf(procDefEntity.getProcDefVer()));
        result.setRootEntity(procDefEntity.getRootEntity());
        result.setStatus(procDefEntity.getStatus());
        result.setExcludeMode(procDefEntity.getExcludeMode());
        // result.setProcDefData(procDefEntity.getProcDefData());
        result.setCreatedTime(formatDate(procDefEntity.getCreatedTime()));

        return result;
    }
    
    protected ProcDefInfoDto doGetProcessDefinition(String id) {
        ProcDefInfoEntity procDefEntity = processDefInfoRepo.selectByPrimaryKey(id);
        if (procDefEntity == null) {
            log.debug("cannot find process def with id {}", id);
            return null;
        }

        ProcDefInfoDto result = procDefInfoDtoFromEntity(procDefEntity);
        result.setProcDefData(procDefEntity.getProcDefData());

        List<TaskNodeDefInfoEntity> taskNodeDefEntities = taskNodeDefInfoRepo.selectAllByProcDefId(id);
        for (TaskNodeDefInfoEntity e : taskNodeDefEntities) {
            TaskNodeDefInfoDto tdto = taskNodeDefInfoDtoFromEntity(e);

            List<TaskNodeParamEntity> taskNodeParamEntities = taskNodeParamRepo.selectAllByProcDefIdAndTaskNodeDefId(id,
                    e.getId());

            for (TaskNodeParamEntity tnpe : taskNodeParamEntities) {
                TaskNodeDefParamDto pdto = taskNodeDefParamDtoFromEntity(tnpe);

                tdto.addParamInfos(pdto);
            }

            result.addTaskNodeInfo(tdto);
        }

        return result;
    }
    
    protected void saveProcRoleBinding(String procId, ProcDefInfoDto procDefInfoDto) throws WecubeCoreException {

        Map<String, List<String>> permissionToRoleMap = procDefInfoDto.getPermissionToRole();

        if (permissionToRoleMap == null || permissionToRoleMap.isEmpty()) {
            throw new WecubeCoreException("3164","There is no process to role with permission mapping found.");
        }

        String errorMsg;
        for (Map.Entry<String, List<String>> permissionToRoleListEntry : permissionToRoleMap.entrySet()) {
            String permissionStr = permissionToRoleListEntry.getKey();

            // check if key is empty or NULL
            if (StringUtils.isBlank(permissionStr)) {
                errorMsg = "The permission key should not be empty or NULL";
                log.error(errorMsg);
                throw new WecubeCoreException("3165",errorMsg);
            }

           

            List<String> roleNameList = permissionToRoleListEntry.getValue();

            // check if roleIdList is NULL
            if (roleNameList == null) {
                errorMsg = String.format("The value of permission: [%s] should not be NULL", permissionStr);
                log.error(errorMsg);
                throw new WecubeCoreException("3294",errorMsg,permissionStr);
            }

            // when permission is MGMT and roleIdList is empty, then it is
            // invalid
            if (ProcRoleBindingEntity.MGMT.equals(permissionStr) && roleNameList.isEmpty()) {
                errorMsg = "At least one role with MGMT role should be declared.";
                log.error(errorMsg);
                throw new WecubeCoreException("3168",errorMsg);
            }
            processRoleService.batchSaveData(procId, roleNameList, permissionStr);
        }
    }
    
    protected TaskNodeDefInfoDto taskNodeDefInfoDtoFromEntity(TaskNodeDefInfoEntity e) {
        TaskNodeDefInfoDto tdto = new TaskNodeDefInfoDto();
        tdto.setDescription(e.getDescription());
        tdto.setNodeDefId(e.getId());
        tdto.setNodeId(e.getNodeId());
        tdto.setNodeName(e.getNodeName());
        tdto.setNodeType(e.getNodeType());
        tdto.setOrderedNo(e.getOrderedNo());
        tdto.setProcDefKey(e.getProcDefKey());
        tdto.setProcDefId(e.getProcDefId());
        tdto.setRoutineExpression(e.getRoutineExp());
        tdto.setRoutineRaw(e.getRoutineRaw());
        tdto.setServiceId(e.getServiceId());
        tdto.setServiceName(e.getServiceName());
        tdto.setStatus(e.getStatus());
        tdto.setTimeoutExpression(e.getTimeoutExp());
        tdto.setTaskCategory(e.getTaskCategory());
        tdto.setPreCheck(e.getPreCheck());
        tdto.setDynamicBind(e.getDynamicBind());
        tdto.setPrevCtxNodeIds(e.getPrevCtxNodeIds());

        return tdto;
    }
    
    protected TaskNodeDefParamDto taskNodeDefParamDtoFromEntity(TaskNodeParamEntity tnpe) {
        TaskNodeDefParamDto pdto = new TaskNodeDefParamDto();
        pdto.setId(tnpe.getId());
        pdto.setNodeId(tnpe.getNodeId());
        pdto.setParamName(tnpe.getParamName());
        pdto.setBindNodeId(tnpe.getBindNodeId());
        pdto.setBindParamName(tnpe.getBindParamName());
        pdto.setBindParamType(tnpe.getBindParamType());
        pdto.setBindType(tnpe.getBindType());
        pdto.setBindValue(tnpe.getBindVal());

        return pdto;
    }
}
