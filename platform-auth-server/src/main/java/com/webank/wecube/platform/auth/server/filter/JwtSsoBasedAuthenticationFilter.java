package com.webank.wecube.platform.auth.server.filter;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtSsoBasedAuthenticationFilter extends BasicAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtSsoBasedAuthenticationFilter.class);

    private static final String SIGNING_KEY = "platform-auth-server-@Jwt!&Secret^#";

    public JwtSsoBasedAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        if(log.isInfoEnabled()){
            log.info("Filter:{} applied", this.getClass().getSimpleName());
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        log.info("======== doFilterInternal  ==========");

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    protected UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization");
        if (StringUtils.isBlank(accessToken)) {
            throw new AuthenticationCredentialsNotFoundException("access token is blank");
        }

        Claims claims = Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(accessToken.replace("Bearer ", ""))
                .getBody();

        String tokenType = claims.get("type", String.class);

        String subject = claims.getSubject();

        log.info("subject:{}", subject);

        if (!"refreshToken".equals(tokenType)) {
            throw new AccessDeniedException("refresh token required");
        }

        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        return new UsernamePasswordAuthenticationToken(subject, null, authorities);

    }

}
