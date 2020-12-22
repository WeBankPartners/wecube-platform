package com.webank.wecube.platform.core.dto.plugin;

import java.util.List;
import java.util.Map;

public class PluginConfigRoleRequestDto {
    private Map<String,List<String>> permissionToRole;

    public Map<String, List<String>> getPermissionToRole() {
        return permissionToRole;
    }

    public void setPermissionToRole(Map<String, List<String>> permissionToRole) {
        this.permissionToRole = permissionToRole;
    }
    

    
}
