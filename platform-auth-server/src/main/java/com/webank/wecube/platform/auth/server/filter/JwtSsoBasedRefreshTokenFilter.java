package com.webank.wecube.platform.auth.server.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.server.authentication.SubSystemAuthenticationToken;
import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.JwtTokenDto;
import com.webank.wecube.platform.auth.server.model.JwtToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoBasedRefreshTokenFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtSsoBasedRefreshTokenFilter.class);

    private static final String URI_REFRESH_TOKEN = "/auth/v1/api/token";

    private AuthenticationManager authenticationManager;

    private static final String HEADER_REFRESH_TOKEN = "Authorization";
    private static final String PREFIX_REFRESH_TOKEN = "Bearer ";

    private ObjectMapper objectMapper = new ObjectMapper();

    private JwtBuilder jwtBuilder = new DefaultJwtBuilder();

    public JwtSsoBasedRefreshTokenFilter(AuthenticationManager authenticationManager) {
        super(new AntPathRequestMatcher(URI_REFRESH_TOKEN, "GET"));

        this.authenticationManager = authenticationManager;

        Assert.notNull(this.authenticationManager, "authentication manager must provide.");

        if (log.isInfoEnabled()) {
            log.info("Filter: {} applied", JwtSsoBasedRefreshTokenFilter.class.getSimpleName());
            log.info("AuthenticationManager: {} applied", authenticationManager.getClass().getSimpleName());
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        log.info("=== refresh token ===");

        validateRequestHeader(request);

        String sRefreshToken = request.getHeader(HEADER_REFRESH_TOKEN);
        sRefreshToken = sRefreshToken.substring(PREFIX_REFRESH_TOKEN.length()).trim();

        if (log.isDebugEnabled()) {
            log.debug("refresh token:{}", sRefreshToken);
        }

        if (StringUtils.isBlank(sRefreshToken)) {
            throw new BadCredentialsException("refresh token is blank.");
        }

        Jws<Claims> jwt = jwtBuilder.parseJwt(sRefreshToken);

        if (jwt == null) {
            log.error("failed to parse refresh token:{}", sRefreshToken);
            throw new BadCredentialsException("bad refresh token.");
        }

        return attemptAuthentication(request, response, jwt);
    }

    protected Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response,
            Jws<Claims> jwt) {

        Claims claims = jwt.getBody();
        validateTokenType(claims);

        String clientType = claims.get(ApplicationConstants.JwtInfo.CLAIM_KEY_CLIENT_TYPE, String.class);
        if (StringUtils.isNotBlank(clientType) && ApplicationConstants.ClientType.SUB_SYSTEM.equals(clientType)) {
            return attemptSubSystemAuthentication(request, response, claims);
        } else {
            return attemptUserAuthentication(request, response, claims);
        }
    }

    protected Authentication attemptUserAuthentication(HttpServletRequest request, HttpServletResponse response,
            Claims claims) {
        
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        
        String username = "dummyUser";
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username, null, authorities);
        //TODO
        return authToken;
    }

    protected Authentication attemptSubSystemAuthentication(HttpServletRequest request, HttpServletResponse response,
            Claims claims) {
        
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        
        String systemCode = "dummyCode";
        
        SubSystemAuthenticationToken token = new SubSystemAuthenticationToken(systemCode,
                null, null, authorities);
        return token;
    }

    protected void validateTokenType(Claims claims) {
        String tokenType = claims.get(ApplicationConstants.JwtInfo.CLAIM_KEY_TYPE, String.class);
        if (!ApplicationConstants.JwtInfo.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            log.error("such token type [{}] is not expected.", tokenType);
            throw new BadCredentialsException("bad refresh token type.");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        JwtTokenDto refreshToken = jwtTokenDto(jwtBuilder.buildRefreshToken(authResult));
        JwtTokenDto accessToken = jwtTokenDto(jwtBuilder.buildAccessToken(authResult));

        response.addHeader("Authorization", "Bearer " + accessToken.getToken());
        response.addHeader("Authentication-Info", "Bearer " + refreshToken.getToken());

        List<JwtTokenDto> dtos = new ArrayList<JwtTokenDto>();
        dtos.add(refreshToken);
        dtos.add(accessToken);

        CommonResponseDto responseBody = CommonResponseDto.okayWithData(dtos);
        String jsonResponseBody = objectMapper.writeValueAsString(responseBody);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().print(jsonResponseBody);
        response.getOutputStream().flush();

    }

    private JwtTokenDto jwtTokenDto(JwtToken t) {
        String expire = String.valueOf(t.getExpiration());
        return new JwtTokenDto(t.getToken(), t.getTokenType(), expire);
    }

    protected void validateRequestHeader(HttpServletRequest request) {
        String header = request.getHeader(HEADER_REFRESH_TOKEN);
        if (header == null || !header.startsWith(PREFIX_REFRESH_TOKEN)) {
            throw new BadCredentialsException("refresh token should provide");
        }
    }

}
