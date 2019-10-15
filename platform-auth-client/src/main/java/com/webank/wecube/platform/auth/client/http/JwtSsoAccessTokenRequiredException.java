package com.webank.wecube.platform.auth.client.http;

import org.springframework.web.client.RestClientException;

public class JwtSsoAccessTokenRequiredException extends RestClientException {

    /**
     * 
     */
    private static final long serialVersionUID = -2067806608960861551L;

    public JwtSsoAccessTokenRequiredException(String msg) {
        super(msg);
    }

    public JwtSsoAccessTokenRequiredException(String msg, Throwable ex) {
        super(msg, ex);
    }

}
