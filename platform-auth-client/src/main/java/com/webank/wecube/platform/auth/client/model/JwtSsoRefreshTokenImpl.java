package com.webank.wecube.platform.auth.client.model;

import java.util.Date;

import com.webank.wecube.platform.auth.client.context.JwtSsoClientContext;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoRefreshTokenImpl extends JwtSsoTokenImpl implements JwtSsoRefreshToken {
    public JwtSsoRefreshTokenImpl(String token, Date expiration) {
        super(token, JwtSsoClientContext.TOKEN_TYPE_REFRESH, expiration);
    }

    public JwtSsoRefreshTokenImpl(String token, long expiration) {
        super(token, JwtSsoClientContext.TOKEN_TYPE_REFRESH, expiration);
    }

}
