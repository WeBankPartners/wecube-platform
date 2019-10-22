package com.webank.wecube.platform.gateway.parser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface JwtSsoTokenParser {
    Jws<Claims> parseJwt(String token);
}
