package com.webank.wecube.platform.auth.client.http.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * @author gavin
 *
 */
@ConfigurationProperties(prefix = "platform.auth")
public class JwtSsoClientProperties {
    private String jwtSsoAuthenticationUri = "http://localhost:8080/auth/v1/api/login";
    private String jwtSsoAccessTokenUri = "http://localhost:8080/auth/v1/api/token";
    private String subSystemCode;
    private String subSystemPrivateKey;
    private String authServerPublicKey;

    public String getJwtSsoAuthenticationUri() {
        return jwtSsoAuthenticationUri;
    }

    public void setJwtSsoAuthenticationUri(String jwtSsoAuthenticationUri) {
        this.jwtSsoAuthenticationUri = jwtSsoAuthenticationUri;
    }

    public String getJwtSsoAccessTokenUri() {
        return jwtSsoAccessTokenUri;
    }

    public void setJwtSsoAccessTokenUri(String jwtSsoAccessTokenUri) {
        this.jwtSsoAccessTokenUri = jwtSsoAccessTokenUri;
    }

    public String getSubSystemCode() {
        return subSystemCode;
    }

    public void setSubSystemCode(String subSystemCode) {
        this.subSystemCode = subSystemCode;
    }

    public String getSubSystemPrivateKey() {
        return subSystemPrivateKey;
    }

    public void setSubSystemPrivateKey(String subSystemPrivateKey) {
        this.subSystemPrivateKey = subSystemPrivateKey;
    }

    public String getAuthServerPublicKey() {
        return authServerPublicKey;
    }

    public void setAuthServerPublicKey(String authServerPublicKey) {
        this.authServerPublicKey = authServerPublicKey;
    }

}
