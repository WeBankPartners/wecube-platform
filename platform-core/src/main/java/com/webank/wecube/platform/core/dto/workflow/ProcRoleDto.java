package com.webank.wecube.platform.core.dto.workflow;

import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;

/**
 * @author howechen
 */
public class ProcRoleDto {
    private String id;
    private String processId;
    private String permission;
    private Long roleId;

    public static ProcRoleDto fromDomain(ProcRoleBindingEntity procRoleBindingEntity) {
        ProcRoleDto result = new ProcRoleDto();
        result.setId(procRoleBindingEntity.getId());
        result.setProcessId(procRoleBindingEntity.getProcId());
        result.setPermission(procRoleBindingEntity.getPermission().toString());
        result.setRoleId(procRoleBindingEntity.getRoleId());
        return result;
    }

    public static ProcRoleBindingEntity toDomain(String procId, Long roleId, ProcRoleBindingEntity.permissionEnum permissionEnum) {
        ProcRoleBindingEntity result = new ProcRoleBindingEntity();
        result.setProcId(procId);
        result.setRoleId(roleId);
        result.setPermission(permissionEnum);
        return result;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
