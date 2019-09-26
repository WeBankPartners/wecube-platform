package com.webank.wecube.platform.auth.server.filter;

import org.springframework.security.core.Authentication;

import com.webank.wecube.platform.auth.server.model.JwtToken;

public interface JwtBuilder {
    
    JwtToken buildRefreshToken(Authentication authentication);
    JwtToken buildAccessToken(Authentication authentication);

}
