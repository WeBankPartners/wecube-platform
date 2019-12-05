package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.dto.CommonResponseDto;

import java.util.List;
import java.util.Map;

public interface UserManagementService {
    CommonResponseDto createUser(Map<String, Object> jsonObject);

    CommonResponseDto retrieveUser();

    CommonResponseDto deleteUser(Long id);

    CommonResponseDto createRole(Map<String, Object> requestBody);

    CommonResponseDto retrieveRole();

    CommonResponseDto deleteRole(Long id);

    CommonResponseDto getRolesByUserName(String userName);

    CommonResponseDto getUsersByRoleId(Long userId);

    CommonResponseDto grantRoleToUsers(Long roleId, List<Object> userIdList);

    CommonResponseDto revokeRoleFromUsers(Long roleId, List<Object> jsonObject);

}
