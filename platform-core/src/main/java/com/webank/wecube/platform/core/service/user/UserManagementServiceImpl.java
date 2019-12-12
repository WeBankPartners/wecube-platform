package com.webank.wecube.platform.core.service.user;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.user.RoleDto;
import com.webank.wecube.platform.core.utils.JsonUtils;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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


    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    public UserManagementServiceImpl(RestTemplate restTemplate, ApplicationProperties applicationProperties) {
        this.restTemplate = restTemplate;
        this.gatewayUrl = applicationProperties.getGatewayUrl();
    }

    @Override
    public CommonResponseDto createUser(String token, Map<String, Object> requestJsonObject) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER_CREATE_URL, requestUrlMap);
        logger.info(String.format("Sending POST request to: [%s] with body: [%s]", requestUrl, requestJsonObject));
        ResponseEntity<String> response = RestTemplateUtils.sendPostRequestWithBody(this.restTemplate, requestUrl, httpHeaders, requestJsonObject);
        return RestTemplateUtils.checkResponse(response);
    }


    @Override
    public CommonResponseDto retrieveUser(String token) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER_RETRIEVE_URL, requestUrlMap);
        logger.info(String.format("Sending GET request to: [%s]", requestUrl));
        ResponseEntity<String> response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto deleteUser(String token, Long id) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(USER_ID_PLACE_HOLDER, String.valueOf(id));
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER_DELETE_URL, requestUrlMap);
        logger.info(String.format("Sending DELETE request to: [%s]", requestUrl));
        ResponseEntity<String> response = RestTemplateUtils.sendDeleteWithoutBody(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto createRole(String token, Map<String, Object> jsonObject) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        String requestUrl = generateRequestUrl(AUTH_SERVER_ROLE_CREATE_URL, requestUrlMap);
        logger.info(String.format("Sending POST request to: [%s] with body: [%s]", requestUrl, jsonObject));
        ResponseEntity<String> response = RestTemplateUtils.sendPostRequestWithBody(this.restTemplate, requestUrl, httpHeaders, jsonObject);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) throws WecubeCoreException {
        String token = "Bearer";
        Map<String, Object> createRoleMap = dtoToMap(roleDto);
        CommonResponseDto createRoleResponse = createRole(token, createRoleMap);
        String createRoleResponseDataJsonString = JsonUtils.toJsonString(createRoleResponse.getData());
        RoleDto resultDto;
        try {
            resultDto = JsonUtils.toObject(createRoleResponseDataJsonString, RoleDto.class);
        } catch (IOException ex) {
            String msg = String.format("Cannot transfer response's data [%s] to RoleId dto", createRoleResponseDataJsonString);
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        return resultDto;
    }

    @Override
    public CommonResponseDto retrieveRole(String token) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        String requestUrl = generateRequestUrl(AUTH_SERVER_ROLE_RETRIEVE_URL, requestUrlMap);
        logger.info(String.format("Sending GET request to: [%s]", requestUrl));
        ResponseEntity<String> response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public List<RoleDto> retrieveRole() throws WecubeCoreException {
        String token = "Bearer";
        CommonResponseDto commonResponseDto = retrieveRole(token);
        String responseDataString = JsonUtils.toJsonString(commonResponseDto.getData());
        List<RoleDto> resultDto;
        try {
            resultDto = JsonUtils.toList(responseDataString, RoleDto.class);
        } catch (IOException ex) {
            String msg = String.format("Cannot transfer response's data [%s] to RoleId dto", commonResponseDto.getData().toString());
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        return resultDto;
    }

    @Override
    public CommonResponseDto deleteRole(String token, Long id) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(ROLE_ID_PLACE_HOLDER, String.valueOf(id));
        String requestUrl = generateRequestUrl(AUTH_SERVER_ROLE_DELETE_URL, requestUrlMap);
        logger.info(String.format("Sending DELETE request to: [%s]", requestUrl));
        ResponseEntity<String> response = RestTemplateUtils.sendDeleteWithoutBody(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public void deleteRole(Long id) {
        String token = "Bearer";
        deleteRole(token, id);
    }

    @Override
    public CommonResponseDto getRolesByUserName(String token, String userName) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(USER_NAME_PLACE_HOLDER, userName);
        String requestUrl = generateRequestUrl(AUTH_SERVER_USER2ROLE_URL, requestUrlMap);
        logger.info(String.format("Sending GET request to: [%s]", requestUrl));
        ResponseEntity<String> response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto getUsersByRoleId(String token, Long roleId) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(ROLE_ID_PLACE_HOLDER, String.valueOf(roleId));
        String requestUrl = generateRequestUrl(AUTH_SERVER_ROLE2USER_URL, requestUrlMap);
        logger.info(String.format("Sending GET request to: [%s]", requestUrl));
        ResponseEntity<String> response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.restTemplate, requestUrl, httpHeaders);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto grantRoleToUsers(String token, Long roleId, List<Object> userIdList) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(ROLE_ID_PLACE_HOLDER, String.valueOf(roleId));
        String requestUrl = generateRequestUrl(AUTH_SERVER_GRANT_URL, requestUrlMap);
        logger.info(String.format("Sending POST request to: [%s] with body: [%s]", requestUrl, userIdList));
        ResponseEntity<String> response = RestTemplateUtils.sendPostRequestWithBody(this.restTemplate, requestUrl, httpHeaders, userIdList);
        return RestTemplateUtils.checkResponse(response);
    }

    @Override
    public CommonResponseDto revokeRoleFromUsers(String token, Long roleId, List<Object> requestObject) {
        HttpHeaders httpHeaders = createHeaderWithToken(token);
        Map<String, String> requestUrlMap = new HashMap<>();
        requestUrlMap.put(GATEWAY_PLACE_HOLDER, this.gatewayUrl);
        requestUrlMap.put(ROLE_ID_PLACE_HOLDER, String.valueOf(roleId));
        String requestUrl = generateRequestUrl(AUTH_SERVER_REVOKE_URL, requestUrlMap);
        logger.info(String.format("Sending DELETE request to: [%s] with body: [%s]", requestUrl, requestObject));
        ResponseEntity<String> response = RestTemplateUtils.sendDeleteWithBody(this.restTemplate, requestUrl, httpHeaders, requestObject);
        return RestTemplateUtils.checkResponse(response);
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> dtoToMap(Object dtoObject) throws WecubeCoreException {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        String dtoJsonString = JsonUtils.toJsonString(dtoObject);
        try {
            resultMap = JsonUtils.toObject(dtoJsonString, resultMap.getClass());
        } catch (IOException ex) {
            String msg = String.format("Cannot transfer response's data [%s] to RoleId dto", dtoObject.toString());
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        return resultMap;
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

    private HttpHeaders createHeaderWithToken(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", token);
        return httpHeaders;
    }
}
