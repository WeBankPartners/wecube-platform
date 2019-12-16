package com.webank.wecube.platform.core.dto.workflow;

public class ProcRoleRequestDto {
    String permission;
    Long roleId;

    public ProcRoleRequestDto() {
    }

    public ProcRoleRequestDto(String permission, Long roleId) {
        this.permission = permission;
        this.roleId = roleId;
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
