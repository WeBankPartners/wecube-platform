package com.webank.wecube.platform.auth.client.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface JwtSsoTokenParser {
    Jws<Claims> parseJwt(String token);
}
