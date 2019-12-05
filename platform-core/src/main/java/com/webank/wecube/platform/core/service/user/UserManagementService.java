package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.CommonResponseDto;

import java.util.List;
import java.util.Map;

public interface UserManagementService {
    CommonResponseDto createUser(String token, Map<String, Object> jsonObject);

    CommonResponseDto retrieveUser(String token);

    CommonResponseDto deleteUser(String token, Long id);

    CommonResponseDto createRole(String token, Map<String, Object> requestBody);

    CommonResponseDto retrieveRole(String token);

    CommonResponseDto deleteRole(String token, Long id);

    CommonResponseDto getRolesByUserName(String token, String userName);

    CommonResponseDto getUsersByRoleId(String token, Long userId);

    CommonResponseDto grantRoleToUsers(String token, Long roleId, List<Object> userIdList);

    CommonResponseDto revokeRoleFromUsers(String token, Long roleId, List<Object> jsonObject);

}
