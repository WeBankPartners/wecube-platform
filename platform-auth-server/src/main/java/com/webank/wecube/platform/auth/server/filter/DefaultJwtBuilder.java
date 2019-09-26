package com.webank.wecube.platform.auth.server.filter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.webank.wecube.platform.auth.server.authentication.SubSystemAuthenticationToken;
import com.webank.wecube.platform.auth.server.common.ApplicationConstants;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class DefaultJwtBuilder implements JwtBuilder {

    private static final String SIGNING_KEY = "platform-auth-server-@Jwt!&Secret^#";

    @Override
    public String buildRefreshToken(Authentication authentication) {

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        determineRefreshTokenDuration(calendar, authentication);
        Date expireTime = calendar.getTime();

        String refreshToken = Jwts.builder().setSubject(authentication.getName()).setIssuedAt(now)
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE, ApplicationConstants.JwtInfo.TOKEN_TYPE_REFRESH)
                .setExpiration(expireTime).signWith(SignatureAlgorithm.HS512, SIGNING_KEY).compact();
        return refreshToken;
    }

    @Override
    public String buildAccessToken(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> strAuthorities = new ArrayList<String>();

        authorities.forEach(a -> strAuthorities.add(a.getAuthority()));

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        determineAccessTokenDuration(calendar, authentication);
        Date expireTime = calendar.getTime();

        String accessToken = Jwts.builder().setSubject(authentication.getName() + "-" + strAuthorities).setIssuedAt(now)
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE, ApplicationConstants.JwtInfo.TOKEN_TYPE_ACCESS)
                .setExpiration(expireTime).signWith(SignatureAlgorithm.HS512, SIGNING_KEY).compact();
        return accessToken;
    }

    protected void determineRefreshTokenDuration(Calendar c, Authentication authentication) {
        if (authentication instanceof SubSystemAuthenticationToken) {
            c.add(Calendar.YEAR, 1);
        } else {
            c.add(Calendar.MINUTE, 3600);
        }
    }

    protected void determineAccessTokenDuration(Calendar c, Authentication authentication) {
        if (authentication instanceof SubSystemAuthenticationToken) {
            c.add(Calendar.YEAR, 1);
        } else {
            c.add(Calendar.MINUTE, 10);
        }
    }

}
