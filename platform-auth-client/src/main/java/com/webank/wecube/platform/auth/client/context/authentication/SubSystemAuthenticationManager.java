package com.webank.wecube.platform.auth.client.context.authentication;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.auth.client.context.JwtSsoClientContext;
import com.webank.wecube.platform.auth.client.encryption.EncryptionUtils;
import com.webank.wecube.platform.auth.client.model.JwtSsoAccessToken;
import com.webank.wecube.platform.auth.client.model.JwtSsoAccessTokenImpl;
import com.webank.wecube.platform.auth.client.model.JwtSsoRefreshToken;
import com.webank.wecube.platform.auth.client.model.JwtSsoRefreshTokenImpl;

/**
 * 
 * @author gavin
 *
 */
public class SubSystemAuthenticationManager implements AuthenticationManager {

    private final RestTemplate restTemplate;

    public SubSystemAuthenticationManager(RestTemplate restTemplate) {
        super();
        this.restTemplate = restTemplate;
    }

    @Override
    public void authenticate(JwtSsoClientContext clientContext) {
        CredentialDto loginDto = new CredentialDto();

        loginDto.setClientType(JwtSsoClientContext.CLIENT_TYPE_SUB_SYSTEM);
        SecureRandom sr = new SecureRandom();
        String nonce = String.valueOf(sr.nextInt(1000));

        loginDto.setNonce(nonce);
        loginDto.setPassword(calculateLoginPassword(clientContext, nonce));
        loginDto.setUsername(clientContext.getSubSystemCode());

        JwtSsoTokenResponse response = restTemplate.postForObject(clientContext.getAuthenticationUri(), loginDto,
                JwtSsoTokenResponse.class);

        handleJwtSsoTokenResponse(clientContext, response);
    }

    @Override
    public void refreshToken(JwtSsoClientContext clientContext) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(JwtSsoClientContext.HEADER_AUTHORIZATION,
                JwtSsoClientContext.PREFIX_BEARER_TOKEN + clientContext.getRefreshToken().getToken());
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<JwtSsoTokenResponse> responseEntity = restTemplate.exchange(clientContext.getAccessTokenUri(),
                HttpMethod.GET, requestEntity, JwtSsoTokenResponse.class);

        handleJwtSsoTokenResponse(clientContext, responseEntity.getBody());

    }

    protected void handleJwtSsoTokenResponse(JwtSsoClientContext clientContext, JwtSsoTokenResponse response) {
        List<JwtSsoTokenDto> tokens = response.getData();

        for (JwtSsoTokenDto token : tokens) {

            if (JwtSsoClientContext.TOKEN_TYPE_ACCESS.equals(token.getTokenType())) {
                JwtSsoAccessToken at = new JwtSsoAccessTokenImpl(token.getToken(), Long.valueOf(token.getExpiration()));
                clientContext.setAcccessToken(at);
            }

            if (JwtSsoClientContext.TOKEN_TYPE_REFRESH.equals(token.getTokenType())) {
                JwtSsoRefreshToken rt = new JwtSsoRefreshTokenImpl(token.getToken(),
                        Long.valueOf(token.getExpiration()));
                clientContext.setRefreshToken(rt);
            }
        }
    }

    protected String calculateLoginPassword(JwtSsoClientContext clientContext, String nonce) {
        String password = String.format("%s:%s", clientContext.getSubSystemCode(), nonce);
        password = EncryptionUtils.encryptByPrivateKeyAsString(password.getBytes(EncryptionUtils.UTF8),
                clientContext.getSubSystemPrivateKey());
        return password;
    }

    static class CredentialDto {

        private String username;
        private String password;
        private String clientType;
        private String nonce;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getClientType() {
            return clientType;
        }

        public void setClientType(String clientType) {
            this.clientType = clientType;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

    }

    static class JwtSsoTokenDto {
        private String expiration;
        private String token;
        private String tokenType;

        public JwtSsoTokenDto() {
            super();
        }

        public JwtSsoTokenDto(String token, String tokenType, String expiration) {
            super();
            this.tokenType = tokenType;
            this.token = token;
            this.expiration = expiration;
        }

        public String getExpiration() {
            return expiration;
        }

        public void setExpiration(String expiration) {
            this.expiration = expiration;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

    }

    static class JwtSsoTokenResponse {
        private String status;
        private String message;
        private List<JwtSsoTokenDto> data;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<JwtSsoTokenDto> getData() {
            return data;
        }

        public void setData(List<JwtSsoTokenDto> data) {
            this.data = data;
        }

    }

}
