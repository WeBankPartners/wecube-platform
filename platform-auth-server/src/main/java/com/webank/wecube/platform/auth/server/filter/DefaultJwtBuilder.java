package com.webank.wecube.platform.auth.server.filter;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.webank.wecube.platform.auth.server.authentication.SubSystemAuthenticationToken;
import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.model.JwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 
 * @author gavin
 *
 */
public class DefaultJwtBuilder implements JwtBuilder {

    private static final String SIGNING_KEY = "platform-auth-server-@Jwt!&Secret^#";

    @Override
    public JwtToken buildRefreshToken(Authentication authentication) {

        Date now = new Date();
        Date expireTime = determineRefreshTokenDuration(now, authentication);
        String clientType = determineClientType(authentication);

        String refreshToken = Jwts //
                .builder() //
                .setSubject(authentication.getName()) //
                .setIssuedAt(now) //
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE, ApplicationConstants.JwtInfo.TOKEN_TYPE_REFRESH) //
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_CLIENT_TYPE, clientType).setExpiration(expireTime) //
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY) //
                .compact(); //

        return new JwtToken(refreshToken, ApplicationConstants.JwtInfo.TOKEN_TYPE_REFRESH, expireTime.getTime());

    }

    @Override
    public JwtToken buildAccessToken(Authentication authentication) {
        String sAuthorities = formatAuthorities(authentication.getAuthorities());

        Date now = new Date();
        Date expireTime = determineAccessTokenDuration(now, authentication);
        String clientType = determineClientType(authentication);

        String accessToken = Jwts //
                .builder() //
                .setSubject(authentication.getName()) //
                .setIssuedAt(now) //
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE, ApplicationConstants.JwtInfo.TOKEN_TYPE_ACCESS) //
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_CLIENT_TYPE, clientType) //
                .setExpiration(expireTime) //
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_AUTHORITIES, sAuthorities) //
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY) //
                .compact(); //
        return new JwtToken(accessToken, ApplicationConstants.JwtInfo.TOKEN_TYPE_ACCESS, expireTime.getTime());
    }
    
    protected String formatAuthorities(Collection<? extends GrantedAuthority> authorities){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        boolean isFirst = true;
        
        for(GrantedAuthority a : authorities){
            if(!isFirst){
                sb.append(",").append(a.getAuthority());
            }else{
                sb.append(a.getAuthority());
                isFirst = false;
            }
        }
        
        
        sb.append("]");
        return sb.toString();
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
