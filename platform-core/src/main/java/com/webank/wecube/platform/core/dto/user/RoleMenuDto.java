package com.webank.wecube.platform.core.dto.user;

import com.webank.wecube.platform.core.dto.MenuItemDto;

import java.util.Collections;
import java.util.List;

public class RoleMenuDto {
    private Long roleId;
    private List<MenuItemDto> menuCodeList;

    public RoleMenuDto(Long roleName, List<MenuItemDto> menuCodeList) {
        this.roleId = roleName;
        this.menuCodeList = menuCodeList;
    }

    public Long getRoleName() {
        return roleId;
    }

    public void setRoleName(Long roleId) {
        this.roleId = roleId;
    }

    public List<MenuItemDto> getMenuCodeList() {
        menuCodeList.sort(MenuItemDto::compareTo);
        return menuCodeList;
    }

    public void setMenuCodeList(List<MenuItemDto> menuCodeList) {
        this.menuCodeList = menuCodeList;
    }
}
