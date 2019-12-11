package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.user.RoleMenuDto;

import java.util.List;

public interface RoleMenuService {
    RoleMenuDto retrieveMenusByRoleId(Long roleId);

    RoleMenuDto updateRoleToMenusByRoleId(Long roleId, List<String> menuCodeList);
}
