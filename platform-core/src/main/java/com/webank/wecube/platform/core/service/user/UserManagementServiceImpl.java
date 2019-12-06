package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.utils.RestTemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    public UserManagementServiceImpl(RestTemplate restTemplate, ApplicationProperties applicationProperties) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = applicationProperties.getGatewayUrl();
    }

    @Override
    public CommonResponseDto createUser(Map<String, Object> requestJsonObject) {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER_CREATE_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendPostRequestWithObject(this.restTemplate, requestUrl, httpHeaders, requestJsonObject);
        return RestTemplateUtils.checkResponse(response);
    }


    @Override
    public CommonResponseDto retrieveUser() {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER_RETRIEVE_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto deleteUser(Long id) {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(USER_ID_PLACE_HOLDER, String.valueOf(id));
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER_DELETE_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendDeleteWithoutBody(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto createRole(Map<String, Object> jsonObject) {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        String requestUrl = generateRequestUrl(AUTH_SERVER_ROLE_CREATE_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendPostRequestWithObject(this.restTemplate, requestUrl, httpHeaders, jsonObject);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto retrieveRole() {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        String requestUrl = generateRequestUrl(AUTH_SERVER_ROLE_RETRIEVE_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto deleteRole(Long id) {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(ROLE_ID_PLACE_HOLDER, String.valueOf(id));
        String requestUrl = generateRequestUrl(AUTH_SERVER_ROLE_DELETE_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendDeleteWithoutBody(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto getRolesByUserName(String userName) {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(USER_NAME_PLACE_HOLDER, userName);
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER2ROLE_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto getUsersByRoleId(Long roleId) {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(ROLE_ID_PLACE_HOLDER, String.valueOf(roleId));
        String requestUrl = generateRequestUrl(AUTH_SERVER_ROLE2USER_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto grantRoleToUsers(Long roleId, List<Object> userIdList) {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(ROLE_ID_PLACE_HOLDER, String.valueOf(roleId));
        String requestUrl = generateRequestUrl(AUTH_SERVER_GRANT_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendPostRequestWithObject(this.restTemplate, requestUrl, httpHeaders, userIdList);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto revokeRoleFromUsers(Long roleId, List<Object> requestObject) {
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(ROLE_ID_PLACE_HOLDER, String.valueOf(roleId));
        String requestUrl = generateRequestUrl(AUTH_SERVER_REVOKE_URL, requestUrlMap);
        ResponseEntity<String> response = RestTemplateUtils.sendDeleteWithBody(this.restTemplate, requestUrl, httpHeaders, requestObject);
        return RestTemplateUtils.checkResponse(response);
    }

    private String generateRequestUrl(String requestUrl, Map<String, String> placeHolderToParamMap) {
        Map<String, String> requestUrlParamMap = new HashMap<>();
        for (Map.Entry<String, String> entry : placeHolderToParamMap.entrySet()) {
            requestUrlParamMap.put(entry.getKey(), entry.getValue());
        }
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(requestUrl);
        UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(requestUrlParamMap);
        return uriComponents.toString();
    }
}
