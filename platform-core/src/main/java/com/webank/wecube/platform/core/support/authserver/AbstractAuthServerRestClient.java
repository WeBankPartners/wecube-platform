package com.webank.wecube.platform.core.support.authserver;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.webank.wecube.platform.auth.client.http.JwtSsoRestTemplate;
import com.webank.wecube.platform.core.http.UserJwtSsoTokenRestTemplate;
import com.webank.wecube.platform.core.support.RestClient;

public abstract class AbstractAuthServerRestClient implements RestClient {
    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    protected UserJwtSsoTokenRestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    @Qualifier("jwtSsoRestTemplate")
    protected JwtSsoRestTemplate jwtSsoRestTemplate;

    @Autowired
    protected AuthServerRestClientProperties clientProperties;
    
    protected String buildFullUriString(String path, String httpScheme, String host, int port) {
        if (StringUtils.isNotBlank(path) && !path.startsWith(URI_PATH_DELIMITER)) {
            path = URI_PATH_DELIMITER + path;
        }
        StringBuilder sb = new StringBuilder().append(httpScheme).append(URI_COMPONENTS_DELIMITER)
                .append(URI_SCHEME_DELIMITER).append(host).append(URI_COMPONENTS_DELIMITER).append(String.valueOf(port))
                .append(path);

        return sb.toString();
    }

    protected void deleteObject(String path, Object... uriVariables) throws AuthServerClientException {
        String requestUri = buildFullUriString(path, clientProperties.getHttpScheme(),
                clientProperties.getHost(), clientProperties.getPort());

        URI expandedUri = userJwtSsoTokenRestTemplate.getUriTemplateHandler().expand(requestUri, uriVariables);

        ResponseEntity<AuthServerRestResponseDto<Object>> responseEntity = userJwtSsoTokenRestTemplate.exchange(
                expandedUri, HttpMethod.DELETE, buildRequestEntity(null),
                new ParameterizedTypeReference<AuthServerRestResponseDto<Object>>() {
                });
        AuthServerRestResponseDto<Object> responseDto = responseEntity.getBody();
        String status = responseDto.getStatus();
        if (!AuthServerRestResponseDto.STATUS_OK.equalsIgnoreCase(status)) {
            getLogger().warn("rest service invocation failed,status={},message={}", responseDto.getStatus(),
                    responseDto.getMessage());
            throw new AuthServerClientException(responseDto.getStatus(), responseDto.getMessage());
        }
    }

    protected <T> T getForObject(String path, ParameterizedTypeReference<AuthServerRestResponseDto<T>> responseType,
            Object... uriVariables) throws AuthServerClientException {
        String requestUri = buildFullUriString(path, clientProperties.getHttpScheme(),
                clientProperties.getHost(), clientProperties.getPort());

        URI expandedUri = userJwtSsoTokenRestTemplate.getUriTemplateHandler().expand(requestUri, uriVariables);
        ResponseEntity<AuthServerRestResponseDto<T>> responseEntity = userJwtSsoTokenRestTemplate.exchange(expandedUri,
                HttpMethod.GET, buildRequestEntity(null), responseType);
        AuthServerRestResponseDto<T> responseDto = responseEntity.getBody();
        String status = responseDto.getStatus();
        if (!AuthServerRestResponseDto.STATUS_OK.equalsIgnoreCase(status)) {
            getLogger().warn("rest service invocation failed,status={},message={}", responseDto.getStatus(),
                    responseDto.getMessage());
            throw new AuthServerClientException(responseDto.getStatus(), responseDto.getMessage());
        }
        return responseDto.getData();
    }

    protected <T> T postForObject(String path, Object request,
            ParameterizedTypeReference<AuthServerRestResponseDto<T>> responseType, Object... uriVariables)
            throws AuthServerClientException {
        String requestUri = buildFullUriString(path, clientProperties.getHttpScheme(),
                clientProperties.getHost(), clientProperties.getPort());

        URI expandedUri = userJwtSsoTokenRestTemplate.getUriTemplateHandler().expand(requestUri, uriVariables);
        ResponseEntity<AuthServerRestResponseDto<T>> responseEntity = userJwtSsoTokenRestTemplate.exchange(expandedUri,
                HttpMethod.POST, buildRequestEntity(request), responseType);
        AuthServerRestResponseDto<T> responseDto = responseEntity.getBody();
        String status = responseDto.getStatus();
        if (!AuthServerRestResponseDto.STATUS_OK.equalsIgnoreCase(status)) {
            getLogger().warn("rest service invocation failed,status={},message={}", responseDto.getStatus(),
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
    
    protected abstract Logger getLogger();
}
