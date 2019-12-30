package com.webank.wecube.platform.core.dto.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProcRoleRequestDto {
    String permission;
    @JsonProperty(value = "roleId")
    List<String> roleIdList;

    public ProcRoleRequestDto() {
    }

    public ProcRoleRequestDto(String permission, List<String> roleIdList) {
        this.permission = permission;
        this.roleIdList = roleIdList;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getRoleIdList() {
        return roleIdList;
    }

    public void setRoleIdList(List<String> roleIdList) {
        this.roleIdList = roleIdList;
    }
}
