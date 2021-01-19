package com.webank.wecube.platform.gateway.authclient.context.authentication;

import com.webank.wecube.platform.gateway.authclient.context.JwtSsoClientContext;

/**
 * 
 * @author gavin
 *
 */
public interface AuthenticationManager {
    void authenticate(JwtSsoClientContext clientContext);
    void refreshToken(JwtSsoClientContext clientContext);
}
