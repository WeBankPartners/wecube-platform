package com.webank.wecube.platform.core.support.authserver;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@EnableConfigurationProperties({ AuthServerRestClientProperties.class })
public class AuthServerRestClient extends AbstractAuthServerRestClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServerRestClient.class);

    private static AuthServerRestClient _INSTANCE;

    @PostConstruct
    public void afterPropertiesSet() {
        _INSTANCE = this;
        if (log.isDebugEnabled()) {
            log.debug("auth server properties:host={},port={}", clientProperties.getHost(), clientProperties.getPort());
        }

    }

    public static AuthServerRestClient instance() {
        if (_INSTANCE == null) {
            throw new RuntimeException(String.format("%s is NOT fully loaded.", AuthServerRestClient.class.getName()));
        }

        return _INSTANCE;
    }

    public void healthCheck() {
        try {
            getForObject(clientProperties.getPathHealthCheck(),
                    new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                    });
        } catch (Exception e) {
            log.warn("Health check failed", e);
            throw new WecubeCoreException("3301", "Auth server health check failed.");
        }
    }

    /**
     * 
     * @param subSystemReq
     * @return
     */
    public SimpleSubSystemDto registerSimpleSubSystem(SimpleSubSystemDto subSystemReq) {
        if (subSystemReq == null) {
            return null;
        }
        SimpleSubSystemDto subSystemAs = postForObject(jwtSsoRestTemplate, clientProperties.getPathRegisterSubSystem(),
                subSystemReq, new ParameterizedTypeReference<AuthServerRestResponseDto<SimpleSubSystemDto>>() {
                });

        return subSystemAs;
    }

    public void revokeAuthoritiesFromRole(String roleId, List<AsAuthorityDto> authorities) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException();
        }

        if (authorities == null || authorities.isEmpty()) {
            return;
        }

        postForObject(clientProperties.getPathRevokeAuthoritiesFromRole(), authorities,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                }, roleId);
    }

    public void configureRoleAuthorities(String roleId, List<AsAuthorityDto> authorities) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException();
        }

        if (authorities == null || authorities.isEmpty()) {
            return;
        }

        postForObject(clientProperties.getPathConfigureRoleAuthorities(), authorities,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                }, roleId);
    }

    public void configureRoleAuthoritiesWithRoleName(AsRoleAuthoritiesDto asRoleAuthoritiesDto) {

        postForObject(clientProperties.getPathConfigureRoleAuthoritiesWithRoleName(), asRoleAuthoritiesDto,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                });
    }

    public void revokeRoleAuthoritiesWithRoleName(AsRoleAuthoritiesDto asRoleAuthoritiesDto) {

        postForObject(clientProperties.getPathRevokeRoleAuthoritiesWithRoleName(), asRoleAuthoritiesDto,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                });
    }

    public void revokeRolesFromUser(String userId, List<AsRoleDto> roleDtos) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException();
        }

        if (roleDtos == null || roleDtos.isEmpty()) {
            return;
        }

        postForObject(clientProperties.getPathRevokeRolesFromUser(), roleDtos,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                }, userId);
    }

    public void configureRolesForUser(String userId, List<AsRoleDto> roleDtos) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException();
        }

        if (roleDtos == null || roleDtos.isEmpty()) {
            return;
        }

        postForObject(clientProperties.getPathConfigureRolesForUser(), roleDtos,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                }, userId);
    }

    public void revokeRoleFromUsers(String roleId, List<AsUserDto> userDtos) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException();
        }

        if (userDtos == null || userDtos.isEmpty()) {
            return;
        }

        postForObject(clientProperties.getPathRevokeRoleFromUsers(), userDtos,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                }, roleId);
    }

    public void configureRoleForUsers(String roleId, List<AsUserDto> asUsers) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException();
        }

        if (asUsers == null || asUsers.isEmpty()) {
            return;
        }

        postForObject(clientProperties.getPathConfigureRoleForUsers(), asUsers,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                }, roleId);
    }

    public List<AsUserDto> retrieveAllUsersBelongsToRoleId(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException();
        }

        List<AsUserDto> asUsers = getForObject(clientProperties.getPathRetrieveAllUsersBelongsToRoleId(),
                new ParameterizedTypeReference<AuthServerRestResponseDto<List<AsUserDto>>>() {
                }, roleId);

        return asUsers;
    }

    public List<AsRoleDto> retrieveAllRoles(String requiredAll) {
        if (StringUtils.isBlank(requiredAll)) {
            requiredAll = "N";
        }

        String path = clientProperties.getPathRetrieveAllRoles() + "?all=" + requiredAll;
        List<AsRoleDto> asRoles = getForObject(path,
                new ParameterizedTypeReference<AuthServerRestResponseDto<List<AsRoleDto>>>() {
                });
        return asRoles;
    }

    public void deleteLocalRoleByRoleId(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException();
        }

        deleteObject(clientProperties.getPathDeleteLocalRoleByRoleId(), roleId);
    }

    public AsRoleDto updateLocalRole(AsRoleDto request) {
        if (request == null) {
            throw new IllegalArgumentException();
        }

        if (StringUtils.isBlank(request.getId())) {
            throw new AuthServerClientException("The ID of role to register cannot be empty.");
        }

        AsRoleDto result = postForObject(clientProperties.getPathUpdateLocalRole(), request,
                new ParameterizedTypeReference<AuthServerRestResponseDto<AsRoleDto>>() {
                });
        return result;
    }

    public AsRoleDto registerLocalRole(AsRoleDto request) {
        if (request == null) {
            throw new IllegalArgumentException();
        }

        if (StringUtils.isBlank(request.getName())) {
            throw new AuthServerClientException("The name of role to register cannot be empty.");
        }

        AsRoleDto result = postForObject(clientProperties.getPathRegisterLocalRole(), request,
                new ParameterizedTypeReference<AuthServerRestResponseDto<AsRoleDto>>() {
                });
        return result;
    }

    public AsRoleDto retrieveRoleById(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            return null;
        }

        AsRoleDto role = getForObject(clientProperties.getPathRetrieveRoleById(),
                new ParameterizedTypeReference<AuthServerRestResponseDto<AsRoleDto>>() {
                }, roleId);
        return role;
    }

    public AsRoleDto retrieveRoleByName(String roleName) {
        if (StringUtils.isBlank(roleName)) {
            return null;
        }

        AsRoleDto role = getForObject(clientProperties.getPathRetrieveRoleByName(),
                new ParameterizedTypeReference<AuthServerRestResponseDto<AsRoleDto>>() {
                }, roleName);
        return role;
    }

    public List<AsRoleDto> retrieveGrantedRolesByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException();
        }

        List<AsRoleDto> result = getForObject(clientProperties.getPathRetrieveGrantedRolesByUsername(),
                new ParameterizedTypeReference<AuthServerRestResponseDto<List<AsRoleDto>>>() {
                }, username);
        return result;
    }

    public AsUserDto getLocalUserByUserId(String userId) {
        AsUserDto result = getForObject(clientProperties.getPathGetUserByUserId(),
                new ParameterizedTypeReference<AuthServerRestResponseDto<AsUserDto>>() {
                }, userId);
        return result;
    }

    public AsUserDto registerLocalUser(AsUserDto asUserDto) {
        AsUserDto result = postForObject(clientProperties.getPathRegisterLocalUser(), asUserDto,
                new ParameterizedTypeReference<AuthServerRestResponseDto<AsUserDto>>() {
                });
        return result;
    }

    public List<AsUserDto> retrieveAllUserAccounts() {
        List<AsUserDto> asUsers = getForObject(clientProperties.getPathRetrieveAllUserAccounts(),
                new ParameterizedTypeReference<AuthServerRestResponseDto<List<AsUserDto>>>() {
                });
        return asUsers;
    }

    public void deleteUserAccountByUserId(String userId) {
        deleteObject(clientProperties.getPathDeleteUserAccountByUserId(), userId);
    }

    public AsUserDto changeUserPassword(AsUserPassDto asUserPassDto) {
        AsUserDto result = postForObject(clientProperties.getPathUserChangePassword(), asUserPassDto,
                new ParameterizedTypeReference<AuthServerRestResponseDto<AsUserDto>>() {
                });
        return result;
    }

    public String resetUserPassword(AsUserPassDto asUserPassDto) {
        String result = postForObject(clientProperties.getPathUserResetPassword(), asUserPassDto,
                new ParameterizedTypeReference<AuthServerRestResponseDto<String>>() {
                });
        return result;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

}
