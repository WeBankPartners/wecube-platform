package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.user.RoleMenuDto;

import java.util.List;

public interface RoleMenuService {
    RoleMenuDto retrieveMenusByRoleId(String roleId);

    void updateRoleToMenusByRoleId(String roleId, List<String> menuCodeList);
}
