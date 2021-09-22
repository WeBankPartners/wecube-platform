package com.webank.wecube.platform.core.service.user;

import java.util.List;

import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.UserDto;
import com.webank.wecube.platform.core.dto.user.UserPasswordDto;
import com.webank.wecube.platform.core.dto.user.UserPasswordResetDto;


public interface UserManagementService {
    String resetUserPassword(UserPasswordResetDto userPassResetDto);
    
    UserDto registerUser(UserDto userDto);
    
    void changeUserPassword(UserPasswordDto userPassDto);

    List<UserDto> retrieveAllUserAccounts();

    void deleteUserByUserId(String userId);
    
    UserDto getUserByUserId(String userId);

    RoleDto registerLocalRole(RoleDto role);
    
    RoleDto updateLocalRole(String roleId, RoleDto role);

    List<RoleDto> retrieveAllRoles();

    RoleDto retrieveRoleById(String roleId);
    
    RoleDto retrieveRoleByRoleName(String roleName);

    void unregisterLocalRoleById(String roleId);

    List<RoleDto> getGrantedRolesByUsername(String username);

    List<UserDto> getUsersByRoleId(String roleId);

    void grantRoleToUsers(String roleId, List<String> userIds);
    
    void grantRolesToUser(String userId, List<String> roleIds);

    void revokeRoleFromUsers(String roleId, List<String> userIds);
    
    void revokeRolesFromUser(String userId, List<String> roleIds);

    List<String> getRoleNamesByUsername(String username);

}
