package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.utils.RestTemplateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserManagementServiceImpl implements UserManagementService {
    // request URLs
    private static final String GATEWAY_PLACE_HOLDER = "gatewayUrl";
    private static final String USER_ID_PLACE_HOLDER = "userId";
    private static final String USER_NAME_PLACE_HOLDER = "userName";
    private static final String ROLE_ID_PLACE_HOLDER = "roleId";
    private static final String AUTH_SERVER_USER_CREATE_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/users";
    private static final String AUTH_SERVER_USER_RETRIEVE_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/users";
    private static final String AUTH_SERVER_USER_DELETE_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/users/{" + USER_ID_PLACE_HOLDER + "}";
    private static final String AUTH_SERVER_ROLE_CREATE_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/roles";
    private static final String AUTH_SERVER_ROLE_RETRIEVE_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/roles";
    private static final String AUTH_SERVER_ROLE_DELETE_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/roles/{" + ROLE_ID_PLACE_HOLDER + "}";
    private static final String AUTH_SERVER_USER2ROLE_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/users/{" + USER_NAME_PLACE_HOLDER + "}/roles";
    private static final String AUTH_SERVER_ROLE2USER_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/roles/{" + ROLE_ID_PLACE_HOLDER + "}/users";
    private static final String AUTH_SERVER_GRANT_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/roles/{" + ROLE_ID_PLACE_HOLDER + "}/users";
    private static final String AUTH_SERVER_REVOKE_URL = "http://{" + GATEWAY_PLACE_HOLDER + "}/auth/v1/roles/{" + ROLE_ID_PLACE_HOLDER + "}/users";
    private String gatewayUrl;
    private RestTemplate restTemplate;
    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    public UserManagementServiceImpl(RestTemplate restTemplate, ApplicationProperties applicationProperties) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = applicationProperties.getGatewayUrl();
    }

    @Override
    public CommonResponseDto createUser(JSONObject requestJsonObject) {
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER_CREATE_URL, this.gatewayUrl);
        ResponseEntity<String> response = RestTemplateUtils.sendPostRequestWithJsonObject(this.restTemplate, requestUrl, httpHeaders, requestJsonObject);
        return null;
    }

    private String generateRequestUrl(String authServerUserCreateUrl, String gatewayUrl) {
        Map<String, String> requestUrlParamMap = new HashMap<>();
        requestUrlParamMap.put(GATEWAY_PLACE_HOLDER, gatewayUrl);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(authServerUserCreateUrl);
        UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(requestUrlParamMap);
        String uriStr = uriComponents.toString();
        return uriStr;
    }

    @Override
    public CommonResponseDto retrieveUser() {
        return null;
    }

    @Override
    public CommonResponseDto deleteUser(Long id) {
        return null;
    }

    @Override
    public CommonResponseDto createRole(JSONObject jsonObject) {
        return null;
    }

    @Override
    public CommonResponseDto retrieveRole() {
        return null;
    }

    @Override
    public CommonResponseDto deleteRole(Long id) {
        return null;
    }

    @Override
    public CommonResponseDto getRolesByUserName(String userName) {
        return null;
    }

    @Override
    public CommonResponseDto getUsersByRoleId(Long userId) {
        return null;
    }

    @Override
    public CommonResponseDto grantRoleForUsers(Long roleId, JSONObject userIdList) {
        return null;
    }

    @Override
    public CommonResponseDto revokeRoleFromUser(Long roleId, JSONObject jsonObject) {
        return null;
    }
}
