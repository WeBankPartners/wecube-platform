package com.webank.wecube.platform.auth.server.filter;

import static com.webank.wecube.platform.auth.server.common.ApplicationConstants.JwtInfo.CLAIM_KEY_AUTHORITIES;
import static com.webank.wecube.platform.auth.server.common.ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE;
import static com.webank.wecube.platform.auth.server.common.ApplicationConstants.JwtInfo.HEADER_AUTHORIZATION;
import static com.webank.wecube.platform.auth.server.common.ApplicationConstants.JwtInfo.PREFIX_BEARER_TOKEN;
import static com.webank.wecube.platform.auth.server.common.ApplicationConstants.JwtInfo.TOKEN_TYPE_ACCESS;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public class JwtSsoBasedAuthenticationFilter extends BasicAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtSsoBasedAuthenticationFilter.class);

    private JwtBuilder jwtBuilder = new DefaultJwtBuilder();

    public JwtSsoBasedAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        if(log.isInfoEnabled()){
            log.info("Filter:{} applied", this.getClass().getSimpleName());
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.info("=== doFilterInternal  ===");

        String header = request.getHeader(HEADER_AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
    
    protected void validateRequestHeader(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIX_BEARER_TOKEN)) {
            throw new BadCredentialsException("Access token is required.");
        }
    }

    protected UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        validateRequestHeader(request);
        
        String sAccessTokenHeader = request.getHeader(HEADER_AUTHORIZATION);
        
        
        String sAccessToken = sAccessTokenHeader.substring(PREFIX_BEARER_TOKEN.length()).trim();
        
        if (StringUtils.isBlank(sAccessToken)) {
            throw new AuthenticationCredentialsNotFoundException("Access token is blank");
        }
        
        Jws<Claims> jwt = jwtBuilder.parseJwt(sAccessToken);

        Claims claims = jwt.getBody();

        String sAuthorities = claims.get(CLAIM_KEY_AUTHORITIES, String.class);

        String username = claims.getSubject();

        log.info("subject:{}", username);
        
        String tokenType = claims.get(CLAIM_KEY_TYPE, String.class);

        if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
            throw new AccessDeniedException("Access token is required.");
        }
        
        if(sAuthorities.length() >= 2){
            sAuthorities  = sAuthorities.substring(1);
            sAuthorities = sAuthorities.substring(0, sAuthorities.length() - 1);
        }

        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        
        if(StringUtils.isNotBlank(sAuthorities)){
            String [] aAuthParts = sAuthorities.split(",");
            for(String s : aAuthParts){
                GrantedAuthority ga = new SimpleGrantedAuthority(s.trim());
                authorities.add(ga);
            }
        }

        return new UsernamePasswordAuthenticationToken(username, sAccessTokenHeader, authorities);

    }

}
