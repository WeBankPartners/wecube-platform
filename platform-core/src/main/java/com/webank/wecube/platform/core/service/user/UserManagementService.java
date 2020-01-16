package com.webank.wecube.platform.core.service.user;

import java.util.List;
import java.util.Map;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.UserDto;

/**
 * @author howechen
 */
public interface UserManagementService {
    UserDto registerUser(UserDto userDto);

    CommonResponseDto retrieveUser(String token);

    CommonResponseDto deleteUser(String token, Long id);

    CommonResponseDto createRole(String token, Map<String, Object> requestBody);

    CommonResponseDto retrieveRole(String token);

    CommonResponseDto retrieveRoleById(String token, String roleId);

    CommonResponseDto deleteRole(String token, String id);

    CommonResponseDto getRolesByUserName(String token, String username);

    CommonResponseDto getUsersByRoleId(String token, String roleId);

    CommonResponseDto grantRoleToUsers(String token, String roleId, List<Object> userIdList);

    CommonResponseDto revokeRoleFromUsers(String token, String roleId, List<Object> jsonObject);

    RoleDto createRole(RoleDto roleDto);

    List<RoleDto> retrieveRole();

    List<String> getRoleIdListByUsername(String token, String username);

    List<RoleDto> getRoleListByUserName(String token, String username);
}
