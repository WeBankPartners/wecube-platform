package com.webank.wecube.platform.core.support.authserver;

import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.client.http.JwtSsoRestTemplate;
import com.webank.wecube.platform.core.http.UserJwtSsoTokenRestTemplate;
import com.webank.wecube.platform.core.support.RestClient;

@Service
@EnableConfigurationProperties({ AuthServerRestClientProperties.class })
public class AuthServerRestClient implements RestClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServerRestClient.class);

    private static AuthServerRestClient _INSTANCE;

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    private UserJwtSsoTokenRestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    @Qualifier("jwtSsoRestTemplate")
    private JwtSsoRestTemplate jwtSsoRestTemplate;

    @Autowired
    private AuthServerRestClientProperties clientProperties;

    @PostConstruct
    public void afterPropertiesSet() {
        _INSTANCE = this;
        if (log.isDebugEnabled()) {
            log.debug("auth server properties:host={},port={}", clientProperties.getHost(),
                    clientProperties.getPort());
        }

    }

    public static AuthServerRestClient instance() {
        if (_INSTANCE == null) {
            throw new RuntimeException(String.format("%s is NOT fully loaded.", AuthServerRestClient.class.getName()));
        }

        return _INSTANCE;
    }

    public void revokeUserRolesById(String roleId, List<Object> userIds) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException();
        }

        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        postForObject(clientProperties.getPathRevokeUserRolesById(), userIds,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                }, roleId);
    }

    public void configureUserRolesById(String roleId, List<Object> userIds) {
        if (StringUtils.isBlank(roleId)) {
            throw new IllegalArgumentException();
        }

        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        postForObject(clientProperties.getPathConfigureUserRolesById(), userIds,
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

    public List<AsRoleDto> retrieveAllRoles() {
        List<AsRoleDto> asRoles = getForObject(clientProperties.getPathRetrieveAllRoles(),
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

    public List<AsRoleDto> retrieveGrantedRolesByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException();
        }

        List<AsRoleDto> result = getForObject(clientProperties.getPathRetrieveGrantedRolesByUsername(),
                new ParameterizedTypeReference<AuthServerRestResponseDto<List<AsRoleDto>>>() {
                }, username);
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

    protected String buildFullUriString(String path, String httpSchema, String host, int port) {
        if (StringUtils.isNotBlank(path) && !path.startsWith(URI_PATH_DELIMITER)) {
            path = URI_PATH_DELIMITER + path;
        }
        StringBuilder sb = new StringBuilder().append(httpSchema).append(URI_COMPONENTS_DELIMITER)
                .append(URI_SCHEMA_DELIMITER).append(host).append(URI_COMPONENTS_DELIMITER).append(String.valueOf(port))
                .append(path);

        return sb.toString();
    }

    protected void deleteObject(String path, Object... uriVariables) throws AuthServerClientException {
        String requestUri = buildFullUriString(path, clientProperties.getHttpSchema(),
                clientProperties.getHost(), clientProperties.getPort());

        URI expandedUri = userJwtSsoTokenRestTemplate.getUriTemplateHandler().expand(requestUri, uriVariables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
        ResponseEntity<AuthServerRestResponseDto<Object>> responseEntity = userJwtSsoTokenRestTemplate.exchange(
                expandedUri, HttpMethod.DELETE, requestEntity,
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                });
        AuthServerRestResponseDto<Object> responseDto = responseEntity.getBody();
        String status = responseDto.getStatus();
        if (!AuthServerRestResponseDto.STATUS_OK.equalsIgnoreCase(status)) {
            log.error("rest service invocation failed,status={},message={}", responseDto.getStatus(),
                    responseDto.getMessage());
            throw new AuthServerClientException(responseDto.getStatus(), responseDto.getMessage());
        }
    }

    protected <T> T getForObject(String path, ParameterizedTypeReference<AuthServerRestResponseDto<T>> responseType,
            Object... uriVariables) throws AuthServerClientException {
        String requestUri = buildFullUriString(path, clientProperties.getHttpSchema(),
                clientProperties.getHost(), clientProperties.getPort());

        URI expandedUri = userJwtSsoTokenRestTemplate.getUriTemplateHandler().expand(requestUri, uriVariables);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(headers);
        ResponseEntity<AuthServerRestResponseDto<T>> responseEntity = userJwtSsoTokenRestTemplate.exchange(expandedUri,
                HttpMethod.GET, requestEntity, responseType);
        AuthServerRestResponseDto<T> responseDto = responseEntity.getBody();
        String status = responseDto.getStatus();
        if (!AuthServerRestResponseDto.STATUS_OK.equalsIgnoreCase(status)) {
            log.error("rest service invocation failed,status={},message={}", responseDto.getStatus(),
                    responseDto.getMessage());
            throw new AuthServerClientException(responseDto.getStatus(), responseDto.getMessage());
        }
        return responseDto.getData();
    }

    protected <T> T postForObject(String path, Object request,
            ParameterizedTypeReference<AuthServerRestResponseDto<T>> responseType, Object... uriVariables)
            throws AuthServerClientException {
        String requestUri = buildFullUriString(path, clientProperties.getHttpSchema(),
                clientProperties.getHost(), clientProperties.getPort());

        URI expandedUri = userJwtSsoTokenRestTemplate.getUriTemplateHandler().expand(requestUri, uriVariables);
        ResponseEntity<AuthServerRestResponseDto<T>> responseEntity = userJwtSsoTokenRestTemplate.exchange(expandedUri,
                HttpMethod.POST, buildRequestEntity(request), responseType);
        AuthServerRestResponseDto<T> responseDto = responseEntity.getBody();
        String status = responseDto.getStatus();
        if (!AuthServerRestResponseDto.STATUS_OK.equalsIgnoreCase(status)) {
            log.error("rest service invocation failed,status={},message={}", responseDto.getStatus(),
                    responseDto.getMessage());
            throw new AuthServerClientException(responseDto.getStatus(), responseDto.getMessage());
        }
        return responseDto.getData();
    }

    protected HttpEntity<Object> buildRequestEntity(Object request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> requestEntity = null;

        if (request != null) {
            requestEntity = new HttpEntity<Object>(request, headers);
        } else {
            requestEntity = new HttpEntity<Object>(headers);
        }

        return requestEntity;
    }
}
