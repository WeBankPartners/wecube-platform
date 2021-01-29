package com.webank.wecube.platform.core.dto.user;

import java.util.ArrayList;
import java.util.List;

import com.webank.wecube.platform.core.dto.plugin.MenuItemDto;

public class RoleMenuDto {
    private String roleId;
    private String roleName;
    private List<MenuItemDto> menuList = new ArrayList<>();

    public RoleMenuDto() {
        super();
    }

    public RoleMenuDto(String roleName, List<MenuItemDto> menuList) {
        this.roleId = roleName;
        this.menuList = menuList;
    }

    public List<MenuItemDto> getMenuList() {
        if (menuList != null) {
            menuList.sort(MenuItemDto::compareTo);
        }
        return menuList;
    }

    public void setMenuList(List<MenuItemDto> menuList) {
        this.menuList = menuList;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
