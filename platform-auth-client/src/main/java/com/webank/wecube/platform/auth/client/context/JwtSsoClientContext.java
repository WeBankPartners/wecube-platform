package com.webank.wecube.platform.auth.client.context;

import com.webank.wecube.platform.auth.client.model.JwtSsoAccessToken;
import com.webank.wecube.platform.auth.client.model.JwtSsoRefreshToken;

public interface JwtSsoClientContext {
    String CLAIM_KEY_TYPE = "type";
    String CLAIM_KEY_CLIENT_TYPE = "clientType";
    String CLAIM_KEY_AUTHORITIES = "authority";
    String TOKEN_TYPE_REFRESH = "refreshToken";
    String TOKEN_TYPE_ACCESS = "accessToken";
    
    String HEADER_AUTHORIZATION_INFO = "Authorization-Info";
    String HEADER_AUTHORIZATION = "Authorization";
    String PREFIX_BEARER_TOKEN = "Bearer ";
    
    String getAuthServerLoginUrl();
    String getAuthServerRefreshTokenUrl();
    String getLoginClientName();
    String getClientPrivateKey();
    String getAuthServerPublicKey();
    
    JwtSsoRefreshToken retrieveRefreshToken();
    JwtSsoAccessToken retrieveAccessToken();
    void setRefreshToken(JwtSsoRefreshToken refreshToken);
    void setAcccessToken(JwtSsoAccessToken accessToken);

}
