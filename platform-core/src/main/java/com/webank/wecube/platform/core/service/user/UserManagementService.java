package com.webank.wecube.platform.core.service.user;

import java.util.List;

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

    List<RoleDto> retrieveAllRoles();

    RoleDto retrieveRoleById(String roleId);
    
    RoleDto retrieveRoleByRoleName(String roleName);

    void unregisterLocalRoleById(String roleId);

    List<RoleDto> getGrantedRolesByUsername(String username);

    List<UserDto> getUsersByRoleId(String roleId);

    void grantRoleToUsers(String roleId, List<String> userIds);

    void revokeRoleFromUsers(String roleId, List<String> userIds);

    List<String> getRoleIdsByUsername(String username);

}
