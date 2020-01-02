package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.user.RoleMenuDto;

import java.util.List;

public interface RoleMenuService {
    RoleMenuDto retrieveMenusByRoleId(String roleId);

    List<RoleMenuDto> getMenusByUserName(String token, String username);

    void updateRoleToMenusByRoleId(String token, String roleId, List<String> menuCodeList);
}
