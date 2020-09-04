package com.webank.wecube.platform.auth.client.filter;

import com.webank.wecube.platform.auth.client.encryption.StringUtilsEx;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

/**
 * 
 * @author gavinli
 *
 */
public class DefaultJwtSsoTokenParser implements JwtSsoTokenParser {

    private static final String SIGNING_KEY = "Platform+Auth+Server+Secret";

    private String jwtSigningKey;

    private JwtParser jwtParser;

    public DefaultJwtSsoTokenParser(String jwtSigningKey) {
        if (jwtSigningKey == null) {
            this.jwtSigningKey = SIGNING_KEY;
        } else {
            this.jwtSigningKey = jwtSigningKey;
        }

        this.jwtParser = Jwts.parser().setSigningKey(StringUtilsEx.decodeBase64(getJwtSigningKey()));
    }

    @Override
    public Jws<Claims> parseJwt(String token) {
        return jwtParser.parseClaimsJws(token);
    }

    private String getJwtSigningKey() {
        return jwtSigningKey;
    }
}
