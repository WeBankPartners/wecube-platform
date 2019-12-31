package com.webank.wecube.platform.core.dto.user;

import com.webank.wecube.platform.core.dto.MenuItemDto;

import java.util.List;

public class RoleMenuDto {
    private String roleId;
    private List<MenuItemDto> menuList;

    public RoleMenuDto(String roleName, List<MenuItemDto> menuList) {
        this.roleId = roleName;
        this.menuList = menuList;
    }

    public String getRoleName() {
        return roleId;
    }

    public void setRoleName(String roleId) {
        this.roleId = roleId;
    }

    public List<MenuItemDto> getMenuList() {
        menuList.sort(MenuItemDto::compareTo);
        return menuList;
    }

    public void setMenuList(List<MenuItemDto> menuList) {
        this.menuList = menuList;
    }
}
