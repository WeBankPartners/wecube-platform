package com.webank.wecube.platform.auth.client.context;

import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.auth.client.context.authentication.AuthenticationManager;
import com.webank.wecube.platform.auth.client.context.authentication.SubSystemAuthenticationManager;
import com.webank.wecube.platform.auth.client.http.configuration.JwtSsoClientProperties;
import com.webank.wecube.platform.auth.client.model.JwtSsoAccessToken;
import com.webank.wecube.platform.auth.client.model.JwtSsoRefreshToken;

/**
 * 
 * @author gavin
 *
 */
public class DefaultJwtSsoClientContext implements JwtSsoClientContext {
    private static final String DEFAULT_VALUE_STRING = "";
    private String jwtSsoAuthenticationUri = DEFAULT_VALUE_STRING;
    private String jwtSsoAccessTokenUri = DEFAULT_VALUE_STRING;
    private String subSystemCode = DEFAULT_VALUE_STRING;
    private String subSystemPrivateKey = DEFAULT_VALUE_STRING;
    private String authServerPublicKey = DEFAULT_VALUE_STRING;

    private JwtSsoRefreshToken refreshToken;
    private JwtSsoAccessToken accessToken;

    private RestTemplate restTemplate;

    private AuthenticationManager authenticationManager;

    public DefaultJwtSsoClientContext(JwtSsoClientProperties jwtSsoClientProperties, RestTemplate restTemplate) {
        super();
        if (jwtSsoClientProperties == null) {
            throw new RuntimeException("auth client properties cannot be null.");
        }

        if (restTemplate == null) {
            throw new RuntimeException("rest template cannot be null.");
        }
        this.restTemplate = restTemplate;

        this.jwtSsoAuthenticationUri = jwtSsoClientProperties.getJwtSsoAuthenticationUri();
        this.jwtSsoAccessTokenUri = jwtSsoClientProperties.getJwtSsoAccessTokenUri();
        this.subSystemCode = jwtSsoClientProperties.getSubSystemCode();
        this.subSystemPrivateKey = jwtSsoClientProperties.getSubSystemPrivateKey();
        this.authServerPublicKey = jwtSsoClientProperties.getAuthServerPublicKey();

        authenticationManager = new SubSystemAuthenticationManager(this.restTemplate);
    }

    @Override
    public String getAuthenticationUri() {
        return jwtSsoAuthenticationUri;
    }

    @Override
    public String getAccessTokenUri() {
        return jwtSsoAccessTokenUri;
    }

    @Override
    public String getSubSystemCode() {
        return subSystemCode;
    }

    @Override
    public String getSubSystemPrivateKey() {
        return subSystemPrivateKey;
    }

    @Override
    public String getAuthServerPublicKey() {
        return authServerPublicKey;
    }

    @Override
    public JwtSsoRefreshToken getRefreshToken() {
        if (refreshToken == null || refreshToken.isExpired()) {
            acquireRefreshToken();
        }
        return refreshToken;
    }

    @Override
    public JwtSsoAccessToken getAccessToken() {
        if (accessToken == null || accessToken.isExpired()) {
            acquireAccessToken(false);
        }

        if (accessToken == null || accessToken.isExpired()) {
            throw new RuntimeException("cannot get access token.");
        }
        return accessToken;
    }

    @Override
    public void setRefreshToken(JwtSsoRefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public void setAcccessToken(JwtSsoAccessToken accessToken) {
        this.accessToken = accessToken;

    }

    protected final synchronized void acquireAccessToken(boolean refreshAnyWay) {
        if (accessToken != null && !accessToken.isExpired() && !refreshAnyWay) {
            return;
        }

        if (refreshToken == null || refreshToken.isExpired()) {
            acquireRefreshToken();
        } else {
            authenticationManager.refreshToken(this);
        }

    }

    protected final synchronized void acquireRefreshToken() {
        if (refreshToken != null && !refreshToken.isExpired()) {
            return;
        }

        authenticationManager.authenticate(this);
    }

    @Override
    public void refreshToken() {
        acquireAccessToken(true);
    }

}
