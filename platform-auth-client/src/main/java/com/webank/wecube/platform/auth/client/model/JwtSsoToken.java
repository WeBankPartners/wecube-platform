package com.webank.wecube.platform.auth.client.model;

import java.util.Date;

/**
 * 
 * @author gavin
 *
 */
public interface JwtSsoToken {
    Date getExpireTime();
    boolean isExpired();
    String getToken();
    String getTokenType();
}
