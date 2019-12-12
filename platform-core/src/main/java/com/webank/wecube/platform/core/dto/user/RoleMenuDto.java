package com.webank.wecube.platform.core.dto.user;

import com.webank.wecube.platform.core.dto.MenuItemDto;

import java.util.List;

public class RoleMenuDto {
    private Long roleId;
    private List<MenuItemDto> menuList;

    public RoleMenuDto(Long roleName, List<MenuItemDto> menuList) {
        this.roleId = roleName;
        this.menuList = menuList;
    }

    public Long getRoleName() {
        return roleId;
    }

    public void setRoleName(Long roleId) {
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
