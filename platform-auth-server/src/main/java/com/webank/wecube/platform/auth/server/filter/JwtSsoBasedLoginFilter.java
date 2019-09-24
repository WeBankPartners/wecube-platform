package com.webank.wecube.platform.auth.server.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.server.dto.CredentialDto;

public class JwtSsoBasedLoginFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtSsoBasedLoginFilter.class);

    private static final String URI_LOGIN = "/v1/api/login";

    private AuthenticationManager authenticationManager;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    public JwtSsoBasedLoginFilter(AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher(URI_LOGIN, "POST"));
        this.authenticationManager = authenticationManager;

        if (log.isInfoEnabled()) {
            log.info("Filter: {} applied", JwtSsoBasedLoginFilter.class.getSimpleName());
            log.info("AuthenticationManager: {} applied", authenticationManager.getClass().getSimpleName());
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        try {
            CredentialDto credential = objectMapper.readValue(request.getInputStream(), CredentialDto.class);
            final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    credential.getUsername(), credential.getPassword());

            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            log.error("errors while reading credential,message:{}", e.getMessage());
            throw e;
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        JwtBuilder jwtBuilder = new DefaultJwtBuilder();
        String refreshToken = jwtBuilder.buildRefreshToken(authResult);
        String accessToken = jwtBuilder.buildAccessToken(authResult);
        
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Authentication-Info", "Bearer " + refreshToken);
    }

}
