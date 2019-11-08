package com.webank.wecube.platform.auth.client.context;

import com.webank.wecube.platform.auth.client.model.JwtSsoAccessToken;
import com.webank.wecube.platform.auth.client.model.JwtSsoRefreshToken;

/**
 * 
 * @author gavin
 *
 */
public interface JwtSsoClientContext {
    String CLAIM_KEY_TYPE = "type";
    String CLAIM_KEY_CLIENT_TYPE = "clientType";
    String CLAIM_KEY_AUTHORITIES = "authority";
    String TOKEN_TYPE_REFRESH = "refreshToken";
    String TOKEN_TYPE_ACCESS = "accessToken";

    String HEADER_AUTHORIZATION_INFO = "Authorization-Info";
    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    String PREFIX_BEARER_TOKEN = "Bearer ";

    String CLIENT_TYPE_SUB_SYSTEM = "SUB_SYSTEM";

    String getAuthenticationUri();

    String getAccessTokenUri();

    String getSubSystemCode();

    String getSubSystemPrivateKey();

    String getAuthServerPublicKey();

    void refreshToken();

    JwtSsoRefreshToken getRefreshToken();

    JwtSsoAccessToken getAccessToken();

    void setRefreshToken(JwtSsoRefreshToken refreshToken);

    void setAcccessToken(JwtSsoAccessToken accessToken);

}
