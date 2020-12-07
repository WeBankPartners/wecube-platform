package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginConfigRoleRequestDto {
    private String permission;
    private List<String> roleIds = new ArrayList<>();

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PluginConfigRoleRequestDto [permission=");
        builder.append(permission);
        builder.append(", roleIds=");
        builder.append(roleIds);
        builder.append("]");
        return builder.toString();
    }

    
}
