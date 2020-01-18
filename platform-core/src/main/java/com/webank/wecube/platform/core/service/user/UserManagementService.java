package com.webank.wecube.platform.core.service.user;

import java.util.List;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.UserDto;

/**
 * @author howechen
 */
public interface UserManagementService {
    UserDto registerUser(UserDto userDto);

    List<UserDto> retrieveAllUserAccounts();

    void deleteUserByUserId(String userId);

    RoleDto registerLocalRole(RoleDto role);

    CommonResponseDto retrieveRole(String token);

    RoleDto retrieveRoleById(String roleId);

    CommonResponseDto deleteRole(String token, String id);

    List<RoleDto> getGrantedRolesByUsername(String username);

    CommonResponseDto getUsersByRoleId(String token, String roleId);

    CommonResponseDto grantRoleToUsers(String token, String roleId, List<Object> userIdList);

    CommonResponseDto revokeRoleFromUsers(String token, String roleId, List<Object> jsonObject);

    List<RoleDto> retrieveRole();

    List<String> getRoleIdsByUsername(String username);

}
