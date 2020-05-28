package com.webank.wecube.platform.auth.server.http.filter;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.webank.wecube.platform.auth.server.authentication.SubSystemAuthenticationToken;
import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;
import com.webank.wecube.platform.auth.server.config.AuthServerProperties;
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

    private static final Logger log = LoggerFactory.getLogger(DefaultJwtBuilder.class);

    private static final String SIGNING_KEY = "Platform+Auth+Server+Secret";

    private AuthServerProperties.JwtTokenProperties jwtTokenProperties;

    private final String signingKey;

    public DefaultJwtBuilder(AuthServerProperties.JwtTokenProperties jwtTokenProperties) {
        this.jwtTokenProperties = jwtTokenProperties;
        this.signingKey = StringUtils.isBlank(jwtTokenProperties.getSigningKey()) ? SIGNING_KEY
                : jwtTokenProperties.getSigningKey();

        log.debug("jwtTokenSettings:{}", jwtTokenProperties);
    }

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
                .signWith(SignatureAlgorithm.HS512, StringUtilsEx.decodeBase64(signingKey)) //
                .compact(); //

        return new JwtToken(refreshToken, ApplicationConstants.JwtInfo.TOKEN_TYPE_REFRESH, expireTime.getTime());

    }
    
    public JwtToken buildAccessToken(Authentication authentication, Date expireTime) {
    	String sAuthorities = formatAuthorities(authentication.getAuthorities());

        Date now = new Date();
        String clientType = determineClientType(authentication);

        String accessToken = Jwts //
                .builder() //
                .setSubject(authentication.getName()) //
                .setIssuedAt(now) //
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE, ApplicationConstants.JwtInfo.TOKEN_TYPE_ACCESS) //
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_CLIENT_TYPE, clientType) //
                .setExpiration(expireTime) //
                .claim(ApplicationConstants.JwtInfo.CLAIM_KEY_AUTHORITIES, sAuthorities) //
                .signWith(SignatureAlgorithm.HS512, StringUtilsEx.decodeBase64(signingKey)) //
                .compact(); //
        return new JwtToken(accessToken, ApplicationConstants.JwtInfo.TOKEN_TYPE_ACCESS, expireTime.getTime());
    }

    @Override
    public JwtToken buildAccessToken(Authentication authentication) {

        Date now = new Date();
        Date expireTime = determineAccessTokenDuration(now, authentication);
        
        return buildAccessToken( authentication,  expireTime);
    }

    protected String formatAuthorities(Collection<? extends GrantedAuthority> authorities) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean isFirst = true;

        for (GrantedAuthority a : authorities) {
            if (!isFirst) {
                sb.append(",").append(a.getAuthority());
            } else {
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
            c.add(Calendar.MINUTE, jwtTokenProperties.getSubSystemRefreshToken());
        } else {
            c.add(Calendar.MINUTE, jwtTokenProperties.getUserRefreshToken());
        }

        return c.getTime();
    }

    protected Date determineAccessTokenDuration(Date now, Authentication authentication) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        if (authentication instanceof SubSystemAuthenticationToken) {
            c.add(Calendar.MINUTE, jwtTokenProperties.getSubSystemAccessToken());
        } else {
            c.add(Calendar.MINUTE, jwtTokenProperties.getUserAccessToken());
        }

        return c.getTime();
    }

    @Override
    public Jws<Claims> parseJwt(String token) {
        return Jwts.parser().setSigningKey(StringUtilsEx.decodeBase64(signingKey)).parseClaimsJws(token);
    }

}
