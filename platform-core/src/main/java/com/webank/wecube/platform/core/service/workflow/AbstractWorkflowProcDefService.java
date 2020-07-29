package com.webank.wecube.platform.core.service.workflow;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefParamDto;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;

public class AbstractWorkflowProcDefService extends AbstractWorkflowService{

    private static final Logger log = LoggerFactory.getLogger(AbstractWorkflowProcDefService.class);
    
    @Autowired
    protected ProcessRoleServiceImpl processRoleService;
    
    protected void saveProcRoleBinding(String procId, ProcDefInfoDto procDefInfoDto) throws WecubeCoreException {

        Map<String, List<String>> permissionToRoleMap = procDefInfoDto.getPermissionToRole();

        if (null == permissionToRoleMap) {
            throw new WecubeCoreException("There is no process to role with permission mapping found.");
        }

        String errorMsg;
        for (Map.Entry<String, List<String>> permissionToRoleListEntry : permissionToRoleMap.entrySet()) {
            String permissionStr = permissionToRoleListEntry.getKey();

            // check if key is empty or NULL
            if (StringUtils.isEmpty(permissionStr)) {
                errorMsg = "The permission key should not be empty or NULL";
                log.error(errorMsg);
                throw new WecubeCoreException(errorMsg);
            }

            // check key is valid permission enum
            if (!EnumUtils.isValidEnum(ProcRoleBindingEntity.permissionEnum.class, permissionStr)) {
                errorMsg = "The request's key is not valid as a permission.";
                log.error(errorMsg);
                throw new WecubeCoreException(errorMsg);
            }

            List<String> roleIdList = permissionToRoleListEntry.getValue();

            // check if roleIdList is NULL
            if (null == roleIdList) {
                errorMsg = String.format("The value of permission: [%s] should not be NULL", permissionStr);
                log.error(errorMsg);
                throw new WecubeCoreException(errorMsg);
            }

            // when permission is MGMT and roleIdList is empty, then it is
            // invalid
            if (ProcRoleBindingEntity.permissionEnum.MGMT.toString().equals(permissionStr) && roleIdList.isEmpty()) {
                errorMsg = "At least one role with MGMT role should be declared.";
                log.error(errorMsg);
                throw new WecubeCoreException(errorMsg);
            }
            processRoleService.batchSaveData(procId, roleIdList, permissionStr);
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
        tdto.setRoutineExpression(e.getRoutineExpression());
        tdto.setRoutineRaw(e.getRoutineRaw());
        tdto.setServiceId(e.getServiceId());
        tdto.setServiceName(e.getServiceName());
        tdto.setStatus(e.getStatus());
        tdto.setTimeoutExpression(e.getTimeoutExpression());
        tdto.setTaskCategory(e.getTaskCategory());

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
        pdto.setBindValue(tnpe.getBindValue());

        return pdto;
    }
}
