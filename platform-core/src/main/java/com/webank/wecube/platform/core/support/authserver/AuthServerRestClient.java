package com.webank.wecube.platform.core.support.authserver;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.auth.client.http.JwtSsoRestTemplate;
import com.webank.wecube.platform.core.http.UserJwtSsoTokenRestTemplate;
import com.webank.wecube.platform.core.support.RestClient;

@Service
@EnableConfigurationProperties({ AuthServerRestClientProperties.class })
public class AuthServerRestClient implements RestClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServerRestClient.class);

    private static AuthServerRestClient INSTANCE;

    private String registerLocalUserPath = "/auth/v1/users";

    @Autowired
    private UserJwtSsoTokenRestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    @Qualifier("jwtSsoRestTemplate")
    private JwtSsoRestTemplate jwtSsoRestTemplate;

    @Autowired
    private AuthServerRestClientProperties authServerRestClientProperties;

    @PostConstruct
    public void afterPropertiesSet() {
        INSTANCE = this;

    }

    public static AuthServerRestClient instance() {
        if (INSTANCE == null) {
            throw new RuntimeException(String.format("%s is NOT loaded.", AuthServerRestClient.class.getName()));
        }

        return INSTANCE;
    }

    public AsUserDto registerLocalUser(AsUserDto asUserDto) {
        AsUserDto result = postForObject(registerLocalUserPath, asUserDto,
                new ParameterizedTypeReference<AuthServerRestResponseDto<AsUserDto>>() {
                });
        return result;
    }

    protected <T> T postForObject(String path, Object request,
            ParameterizedTypeReference<AuthServerRestResponseDto<T>> responseType) {
        String query = null;
        URI requestUri = null;
        try {
            requestUri = new URI(authServerRestClientProperties.getHttpSchema(), null,
                    authServerRestClientProperties.getHost(), authServerRestClientProperties.getPort(), path, query,
                    null);
        } catch (URISyntaxException e) {
            log.error("building request URI errors", e);
            throw new AuthServerClientException();
        }
        ResponseEntity<AuthServerRestResponseDto<T>> responseEntity = userJwtSsoTokenRestTemplate.exchange(requestUri,
                HttpMethod.POST, null, responseType);
        AuthServerRestResponseDto<T> responseDto = responseEntity.getBody();
        String status = responseDto.getStatus();
        if (AuthServerRestResponseDto.STATUS_OK.equalsIgnoreCase(status)) {
            throw new AuthServerClientException(responseDto.getStatus(), responseDto.getMessage());
        }
        return responseDto.getData();
    }
}
