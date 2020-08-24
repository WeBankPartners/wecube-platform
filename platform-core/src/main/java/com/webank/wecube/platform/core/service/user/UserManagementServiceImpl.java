package com.webank.wecube.platform.core.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.dto.user.UserDto;
import com.webank.wecube.platform.core.dto.user.UserPasswordDto;
import com.webank.wecube.platform.core.service.SystemVariableService;
import com.webank.wecube.platform.core.support.RestClientException;
import com.webank.wecube.platform.core.support.authserver.AsRoleDto;
import com.webank.wecube.platform.core.support.authserver.AsUserDto;
import com.webank.wecube.platform.core.support.authserver.AsUserPassDto;
import com.webank.wecube.platform.core.support.authserver.AuthServerClientException;
import com.webank.wecube.platform.core.support.authserver.AuthServerRestClient;

@Service
public class UserManagementServiceImpl implements UserManagementService {
    private final static Logger log = LoggerFactory.getLogger(UserManagementServiceImpl.class);
    
    public static final String SYS_VAR_UM_CTX = "UM_AUTH_CONTEXT";
    public static final String AUTH_TYPE_LOCAL = "LOCAL";
    public static final String AUTH_TYPE_UM = "UM";

    @Autowired
    private AuthServerRestClient authServerRestClient;
    
    @Autowired
    private SystemVariableService systemVariableService;
    
    public void changeUserPassword(UserPasswordDto userPassDto){
        AsUserPassDto asUserPassDto = new AsUserPassDto();
        asUserPassDto.setUsername(AuthenticationContextHolder.getCurrentUsername());
        asUserPassDto.setOriginalPassword(userPassDto.getOriginalPassword());
        asUserPassDto.setChangedPassword(userPassDto.getNewPassword());
        
        authServerRestClient.changeUserPassword(asUserPassDto);
    }
    
    public RoleDto retrieveRoleByRoleName(String roleName){
        if(StringUtils.isBlank(roleName)){
            return null;
        }
        
        try {
            AsRoleDto asRole = authServerRestClient.retrieveRoleByName(roleName);
            if (asRole == null) {
                throw new WecubeCoreException("3269","No such role.");
            }

            RoleDto r = new RoleDto();
            r.setDisplayName(asRole.getDisplayName());
            r.setEmail(asRole.getEmail());
            r.setId(asRole.getId());
            r.setName(asRole.getName());

            return r;
        } catch (RestClientException e) {
            log.error("retrieving role error", e);
            throw new WecubeCoreException(e.getErrorMessage());
        }
    }

    @Override
    public UserDto registerUser(UserDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException();
        }

        if (StringUtils.isBlank(userDto.getUsername())) {
            throw new WecubeCoreException("3027","Username cannot be blank.");
        }

        AsUserDto reqUserDto = new AsUserDto();
        reqUserDto.setUsername(userDto.getUsername());
        reqUserDto.setPassword(userDto.getPassword());
        
        String authType = userDto.getAuthType();
        if(StringUtils.isBlank(authType)) {
        	authType = AUTH_TYPE_LOCAL;
        }
        
        reqUserDto.setAuthSource(authType);
        
        String authContext = tryCalculateAuthContext(authType);
        reqUserDto.setAuthContext(authContext);

        try {
            AsUserDto respUserDto = authServerRestClient.registerLocalUser(reqUserDto);

            UserDto result = new UserDto();
            result.setUsername(respUserDto.getUsername());
            result.setPassword(respUserDto.getPassword());
            result.setAuthType(respUserDto.getAuthSource());
            result.setId(respUserDto.getId());
            return result;
        } catch (RestClientException e) {
            log.error("registering user failed", e);
            throw new WecubeCoreException("3028","Failed to register user,caused by: " + e.getErrorMessage(), e.getErrorMessage());
        }
    }
    
    private String tryCalculateAuthContext(String authType) {
    	if(StringUtils.isBlank(authType)) {
    		return null;
    	}
    	
    	if(AUTH_TYPE_LOCAL.equalsIgnoreCase(authType)) {
    		return null;
    	}
    	
    	if(AUTH_TYPE_UM.equalsIgnoreCase(authType)) {
    		return tryCalculateUmAuthContext();
    	}
    	
    	return null;
    }
    
    private String tryCalculateUmAuthContext() {
    	List<SystemVariable> sysVars = systemVariableService.getGlobalSystemVariableByName(SYS_VAR_UM_CTX);
    	if(sysVars == null || sysVars.isEmpty()) {
    	    String msg = String.format("System variable %s does NOT exist and UM authentication is not supported currently.", SYS_VAR_UM_CTX);
    		throw new WecubeCoreException("3029",msg, SYS_VAR_UM_CTX);
    	}
    	
    	String authCtx = getSystemVariableValue(sysVars.get(0));
    	if(StringUtils.isBlank(authCtx)) {
    	    String msg = String.format("The value of system variable %s is blank and UM authentication is not supported currently..", SYS_VAR_UM_CTX);
    		throw new WecubeCoreException("3030",msg, SYS_VAR_UM_CTX);
    	}
    	
    	return authCtx;
    	
    }
    
    private String getSystemVariableValue(SystemVariable var){
        String varVal = var.getValue();
        if(StringUtils.isBlank(varVal)){
            varVal = var.getDefaultValue();
        }
        
        return varVal;
    }

    @Override
    public List<UserDto> retrieveAllUserAccounts() {
        try {
            List<AsUserDto> asUserDtos = authServerRestClient.retrieveAllUserAccounts();
            List<UserDto> userDtos = new ArrayList<>();
            asUserDtos.forEach(m -> {
                UserDto u = new UserDto();
                u.setId(m.getId());
                u.setUsername(m.getUsername());
                u.setPassword(m.getPassword());

                userDtos.add(u);
            });

            return userDtos;
        } catch (RestClientException e) {
            log.error("failed to retrieve all user accounts", e);
            throw new WecubeCoreException("3031","Failed to retrieve all user accounts.");
        }
    }

    @Override
    public void deleteUserByUserId(String userId) {
        try {
            authServerRestClient.deleteUserAccountByUserId(userId);
        } catch (RestClientException e) {
            log.error("failed to delete user account by user id", e);
            throw new WecubeCoreException("3032","Failed to delete user account.");
        }
    }

    @Override
    public RoleDto registerLocalRole(RoleDto roleDto) {
        if (roleDto == null) {
            throw new IllegalArgumentException();
        }

        if (StringUtils.isBlank(roleDto.getName())) {
            throw new WecubeCoreException("3020","The name of role to register cannot be blank.");
        }

        AsRoleDto requestDto = new AsRoleDto();
        requestDto.setDisplayName(roleDto.getDisplayName());
        requestDto.setEmail(roleDto.getEmail());
        requestDto.setName(roleDto.getName());

        try {
            AsRoleDto result = authServerRestClient.registerLocalRole(requestDto);
            RoleDto retRoleDto = new RoleDto();
            if (result != null) {
                retRoleDto.setDisplayName(result.getDisplayName());
                retRoleDto.setEmail(result.getEmail());
                retRoleDto.setId(result.getId());
                retRoleDto.setName(result.getName());
            }

            return retRoleDto;
        } catch (RestClientException e) {
            log.error("Failed to register local role", e);
            throw new WecubeCoreException(e.getErrorMessage());
        }
    }

    @Override
    public List<RoleDto> retrieveAllRoles() {
        List<AsRoleDto> asRoles = null;
        try {
            asRoles = authServerRestClient.retrieveAllRoles();

        } catch (RestClientException e) {
            log.error("retrieve all roles errors", e);
            throw new WecubeCoreException(e.getErrorMessage());
        }

        List<RoleDto> roles = new ArrayList<>();
        if (asRoles == null || asRoles.isEmpty()) {
            return roles;
        }

        asRoles.forEach(ar -> {
            RoleDto r = new RoleDto();
            r.setDisplayName(ar.getDisplayName());
            r.setEmail(ar.getEmail());
            r.setId(ar.getId());
            r.setName(ar.getName());

            roles.add(r);
        });

        return roles;

    }

    @Override
    public RoleDto retrieveRoleById(String roleId) {
        try {
            AsRoleDto asRole = authServerRestClient.retrieveRoleById(roleId);
            if (asRole == null) {
                throw new WecubeCoreException("3021","No such role.");
            }

            RoleDto r = new RoleDto();
            r.setDisplayName(asRole.getDisplayName());
            r.setEmail(asRole.getEmail());
            r.setId(asRole.getId());
            r.setName(asRole.getName());

            return r;
        } catch (RestClientException e) {
            log.error("retrieving role error", e);
            throw new WecubeCoreException(e.getErrorMessage());
        }
    }

    @Override
    public void unregisterLocalRoleById(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            throw new WecubeCoreException("3022","The ID of role to unregister cannot be blank.");
        }

        try {
            authServerRestClient.deleteLocalRoleByRoleId(roleId);
        } catch (RestClientException e) {
            log.error("errors to unregister local role", e);
            throw new WecubeCoreException(e.getErrorMessage());
        }

    }

    @Override
    public List<RoleDto> getGrantedRolesByUsername(String username) {
        List<RoleDto> grantedRoles = new ArrayList<>();
        if (StringUtils.isBlank(username)) {
            return grantedRoles;
        }

        List<AsRoleDto> asRoles = authServerRestClient.retrieveGrantedRolesByUsername(username);
        if (asRoles == null || asRoles.isEmpty()) {
            return grantedRoles;
        }

        asRoles.forEach(ar -> {
            RoleDto r = new RoleDto();
            r.setDisplayName(ar.getDisplayName());
            r.setEmail(ar.getEmail());
            r.setId(ar.getId());
            r.setName(ar.getName());

            grantedRoles.add(r);
        });

        return grantedRoles;
    }

    @Override
    public List<String> getRoleIdsByUsername(String username) {
        List<RoleDto> roleListByUserName = this.getGrantedRolesByUsername(username);
        return roleListByUserName.stream().map(RoleDto::getId).collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getUsersByRoleId(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            throw new WecubeCoreException("3023","The role ID to retrieve users cannot be blank.");
        }

        List<AsUserDto> asUsers = null;

        try {
            asUsers = authServerRestClient.retrieveAllUsersBelongsToRoleId(roleId);

        } catch (AuthServerClientException e) {
            log.error("retrieve users errors", e);
            throw new WecubeCoreException(e.getErrorMessage());
        }

        List<UserDto> users = new ArrayList<>();
        if (asUsers == null || asUsers.isEmpty()) {
            return users;
        }

        asUsers.forEach(au -> {
            UserDto u = new UserDto();
            u.setUsername(au.getUsername());
            u.setId(au.getId());
            u.setPassword(au.getPassword());

            users.add(u);
        });

        return users;
    }

    @Override
    public void grantRoleToUsers(String roleId, List<String> userIds) {

        if (StringUtils.isBlank(roleId)) {
            throw new WecubeCoreException("3024","Role ID cannot be blank.");
        }

        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        List<AsUserDto> asUsers = new ArrayList<>();
        userIds.forEach(m -> {
            AsUserDto asUser = new AsUserDto();
            asUser.setId(m);
            //
            asUsers.add(asUser);
        });

        try {
            authServerRestClient.configureUserRolesById(roleId, asUsers);
        } catch (AuthServerClientException e) {
            log.error("errors to grant role to users", e);
            throw new WecubeCoreException(e.getErrorMessage());
        }

    }

    @Override
    public void revokeRoleFromUsers(String roleId, List<String> userIds) {
        if (StringUtils.isBlank(roleId)) {
            throw new WecubeCoreException("3033","Role ID cannot be blank.");
        }

        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        List<AsUserDto> asUsers = new ArrayList<>();
        userIds.forEach(m -> {
            AsUserDto asUser = new AsUserDto();
            asUser.setId(m);
            //
            asUsers.add(asUser);
        });

        try {
            authServerRestClient.revokeUserRolesById(roleId, asUsers);
        } catch (AuthServerClientException e) {
            log.error("errors to revoke role from users", e);
            throw new WecubeCoreException(e.getErrorMessage());
        }
        
    }

}
