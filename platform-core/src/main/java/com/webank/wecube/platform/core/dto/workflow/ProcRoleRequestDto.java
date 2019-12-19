package com.webank.wecube.platform.core.dto.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProcRoleRequestDto {
    String permission;
    @JsonProperty(value = "roleId")
    List<Long> roleIdList;

    public ProcRoleRequestDto() {
    }

    public ProcRoleRequestDto(String permission, List<Long> roleIdList) {
        this.permission = permission;
        this.roleIdList = roleIdList;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<Long> getRoleIdList() {
        return roleIdList;
    }

    public void setRoleIdList(List<Long> roleIdList) {
        this.roleIdList = roleIdList;
    }
}
