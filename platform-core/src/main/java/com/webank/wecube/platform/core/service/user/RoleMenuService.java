package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.user.RoleMenuDto;

import java.util.List;

public interface RoleMenuService {
    RoleMenuDto retrieveMenusByRoleId(String roleId);

    List<RoleMenuDto> getMenusByUsername(String username);

    List<String> getMenuCodeListByRoleName(String roleName);

    void updateRoleToMenusByRoleId(String roleId, List<String> menuCodeList);

    void createRoleMenuBinding(String roleName, String menuCode);
}
