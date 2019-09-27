package com.webank.wecube.platform.auth.client.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

public class DefaultWeJwtParser implements WeJwtParser {

    private static final String SIGNING_KEY = "platform-auth-server-@Jwt!&Secret^#";

    @Override
    public Jws<Claims> parseJwt(String token) {
        return Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(token);
    }

}
