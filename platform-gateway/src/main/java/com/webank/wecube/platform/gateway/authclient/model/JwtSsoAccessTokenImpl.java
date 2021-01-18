package com.webank.wecube.platform.gateway.authclient.model;

import java.util.Date;

import com.webank.wecube.platform.gateway.authclient.context.JwtSsoClientContext;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoAccessTokenImpl extends JwtSsoTokenImpl implements JwtSsoAccessToken {
    public JwtSsoAccessTokenImpl(String token, Date expiration) {
        super(token, JwtSsoClientContext.TOKEN_TYPE_ACCESS, expiration);
    }

    public JwtSsoAccessTokenImpl(String token, long expiration) {
        super(token, JwtSsoClientContext.TOKEN_TYPE_ACCESS, expiration);
    }

}
