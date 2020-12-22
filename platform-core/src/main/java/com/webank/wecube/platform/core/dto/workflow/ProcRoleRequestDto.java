package com.webank.wecube.platform.core.dto.workflow;

import java.util.List;
import java.util.Map;

public class ProcRoleRequestDto {
    private Map<String,List<String>> permissionToRole;

    public Map<String, List<String>> getPermissionToRole() {
        return permissionToRole;
    }

    public void setPermissionToRole(Map<String, List<String>> permissionToRole) {
        this.permissionToRole = permissionToRole;
    }
   
   

    
}
