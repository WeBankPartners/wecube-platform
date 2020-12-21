package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcRoleRequestDto {
    private String permission;
    @JsonProperty(value = "roleId")
    private List<String> roleNames = new ArrayList<>();
    public String getPermission() {
        return permission;
    }
    public void setPermission(String permission) {
        this.permission = permission;
    }
    public List<String> getRoleNames() {
        return roleNames;
    }
    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    
}
