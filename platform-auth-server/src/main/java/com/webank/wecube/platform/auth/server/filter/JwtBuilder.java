package com.webank.wecube.platform.auth.server.filter;

import org.springframework.security.core.Authentication;

import com.webank.wecube.platform.auth.server.model.JwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * 
 * @author gavin
 *
 */
public interface JwtBuilder {
    
    JwtToken buildRefreshToken(Authentication authentication);
    JwtToken buildAccessToken(Authentication authentication);

    Jws<Claims> parseJwt(String token);
}
