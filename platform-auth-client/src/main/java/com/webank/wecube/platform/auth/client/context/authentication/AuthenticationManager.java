package com.webank.wecube.platform.auth.client.context.authentication;

import com.webank.wecube.platform.auth.client.context.JwtSsoClientContext;

/**
 * 
 * @author gavin
 *
 */
public interface AuthenticationManager {
    void authenticate(JwtSsoClientContext clientContext);
    void refreshToken(JwtSsoClientContext clientContext);
}
