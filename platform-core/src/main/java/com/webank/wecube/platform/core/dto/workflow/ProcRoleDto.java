package com.webank.wecube.platform.core.dto.workflow;

import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author howechen
 */
public class ProcRoleDto {
    private String id;
    private String processId;
    private String permission;
    private String roleId;
    private String roleName;

    public static ProcRoleDto fromDomain(ProcRoleBindingEntity procRoleBindingEntity) {
        ProcRoleDto result = new ProcRoleDto();
        result.setId(procRoleBindingEntity.getId());
        result.setProcessId(procRoleBindingEntity.getProcId());
        result.setPermission(procRoleBindingEntity.getPermission().toString());
        result.setRoleId(procRoleBindingEntity.getRoleId());
        result.setRoleName(procRoleBindingEntity.getRoleName());
        return result;
    }

    public static ProcRoleBindingEntity toDomain(String procId, String roleId, String permissionEnum, String roleName) {
        ProcRoleBindingEntity result = new ProcRoleBindingEntity();
        
        result.setProcId(procId);
        result.setRoleId(roleId);
        result.setPermission(permissionEnum);
        result.setRoleName(roleName);
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

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProcRoleDto that = (ProcRoleDto) o;

        return new EqualsBuilder()
                .append(getProcessId(), that.getProcessId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getProcessId())
                .toHashCode();
    }
}
