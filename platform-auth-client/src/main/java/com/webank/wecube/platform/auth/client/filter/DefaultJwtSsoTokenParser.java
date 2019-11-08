package com.webank.wecube.platform.auth.client.filter;

import com.webank.wecube.platform.auth.client.encryption.StringUtilsEx;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

public class DefaultJwtSsoTokenParser implements JwtSsoTokenParser {

    private static final String SIGNING_KEY = "Platform+Auth+Server+Secret";

    @Override
    public Jws<Claims> parseJwt(String token) {
        return Jwts.parser().setSigningKey(StringUtilsEx.decodeBase64(SIGNING_KEY)).parseClaimsJws(token);
    }

}
