package com.webank.wecube.platform.auth.server.filter;

import org.springframework.security.core.Authentication;

public interface JwtBuilder {
    
    String buildRefreshToken(Authentication authentication);
    String buildAccessToken(Authentication authentication);

}
