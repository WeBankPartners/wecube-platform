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
import com.webank.wecube.platform.auth.server.model.JwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class DefaultJwtBuilder implements JwtBuilder {

    private static final String SIGNING_KEY = "platform-auth-server-@Jwt!&Secret^#";

    @Override
    public JwtToken buildRefreshToken(Authentication authentication) {

        Date now = new Date();
        Date expireTime = determineRefreshTokenDuration(now, authentication);
        String clientType = determineClientType(authentication);

        String refreshToken = Jwts.builder().setSubject(authentication.getName()).setIssuedAt(now)
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE, ApplicationConstants.JwtInfo.TOKEN_TYPE_REFRESH)
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_CLIENT_TYPE, clientType).setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY).compact();

        return new JwtToken(refreshToken, ApplicationConstants.JwtInfo.TOKEN_TYPE_REFRESH, expireTime.getTime());

    }

    @Override
    public JwtToken buildAccessToken(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> strAuthorities = new ArrayList<String>();

        authorities.forEach(a -> strAuthorities.add(a.getAuthority()));

        Date now = new Date();
        Date expireTime = determineAccessTokenDuration(now, authentication);
        String clientType = determineClientType(authentication);

        String accessToken = Jwts.builder().setSubject(authentication.getName() + "-" + strAuthorities).setIssuedAt(now)
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE, ApplicationConstants.JwtInfo.TOKEN_TYPE_ACCESS)
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_CLIENT_TYPE, clientType).setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY).compact();
        return new JwtToken(accessToken, ApplicationConstants.JwtInfo.TOKEN_TYPE_ACCESS, expireTime.getTime());
    }

    protected String determineClientType(Authentication authentication) {
        if (authentication instanceof SubSystemAuthenticationToken) {
            return ApplicationConstants.ClientType.SUB_SYSTEM;
        } else {
            return ApplicationConstants.ClientType.USER;
        }
    }

    protected Date determineRefreshTokenDuration(Date now, Authentication authentication) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        if (authentication instanceof SubSystemAuthenticationToken) {
            c.add(Calendar.YEAR, 1);
        } else {
            c.add(Calendar.MINUTE, 3600);
        }

        return c.getTime();
    }

    protected Date determineAccessTokenDuration(Date now, Authentication authentication) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        if (authentication instanceof SubSystemAuthenticationToken) {
            c.add(Calendar.YEAR, 1);
        } else {
            c.add(Calendar.MINUTE, 10);
        }

        return c.getTime();
    }

    @Override
    public Jws<Claims> parseJwt(String token) {
        return Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(token);
    }

}
