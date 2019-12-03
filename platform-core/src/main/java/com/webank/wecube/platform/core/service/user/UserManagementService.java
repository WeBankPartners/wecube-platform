package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import org.springframework.boot.configurationprocessor.json.JSONObject;

public interface UserManagementService {
    CommonResponseDto createUser(JSONObject jsonObject);

    CommonResponseDto retrieveUser();

    CommonResponseDto deleteUser(Long id);

    CommonResponseDto createRole(JSONObject jsonObject);

    CommonResponseDto retrieveRole();

    CommonResponseDto deleteRole(Long id);

    CommonResponseDto getRolesByUserName(String userName);

    CommonResponseDto getUsersByRoleId(Long userId);

    CommonResponseDto grantRoleForUsers(Long roleId, JSONObject userIdList);

    CommonResponseDto revokeRoleFromUser(Long roleId, JSONObject jsonObject);

}
