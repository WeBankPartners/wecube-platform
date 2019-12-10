package com.webank.wecube.platform.core.dto.user;

import java.util.List;

public class RoleMenuDto {
    private Long roleId;
    private List<String> menuCodeList;

    public RoleMenuDto(Long roleName, List<String> menuCodeList) {
        this.roleId = roleName;
        this.menuCodeList = menuCodeList;
    }

    public Long getRoleName() {
        return roleId;
    }

    public void setRoleName(Long roleId) {
        this.roleId = roleId;
    }

    public List<String> getMenuCodeList() {
        return menuCodeList;
    }

    public void setMenuCodeList(List<String> menuCodeList) {
        this.menuCodeList = menuCodeList;
    }
}
