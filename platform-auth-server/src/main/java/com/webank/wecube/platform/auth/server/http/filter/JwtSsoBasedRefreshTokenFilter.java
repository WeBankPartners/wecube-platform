package com.webank.wecube.platform.auth.server.http.filter;

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
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.server.authentication.SubSystemAuthenticationToken;
import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.config.AuthServerProperties;
import com.webank.wecube.platform.auth.server.config.SpringApplicationContextUtil;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.JwtTokenDto;
import com.webank.wecube.platform.auth.server.model.JwtToken;
import com.webank.wecube.platform.auth.server.model.SysSubSystemInfo;
import com.webank.wecube.platform.auth.server.service.SubSystemInfoDataService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoBasedRefreshTokenFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtSsoBasedRefreshTokenFilter.class);

    private static final String URI_REFRESH_TOKEN = "/v1/api/token";

    private final AuthenticationManager authenticationManager;


    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final AuthServerProperties authServerProperties;

    private final JwtBuilder jwtBuilder;

    public JwtSsoBasedRefreshTokenFilter(AuthenticationManager authenticationManager, AuthServerProperties authServerProperties) {
        super(new AntPathRequestMatcher(URI_REFRESH_TOKEN, "GET"));

        this.authenticationManager = authenticationManager;
        this.authServerProperties = authServerProperties;

        Assert.notNull(this.authenticationManager, "authentication manager must provide.");

        if (log.isInfoEnabled()) {
            log.info("Filter: {} applied", JwtSsoBasedRefreshTokenFilter.class.getSimpleName());
            log.info("AuthenticationManager: {} applied", authenticationManager.getClass().getSimpleName());
        }
        
        jwtBuilder = new DefaultJwtBuilder(this.authServerProperties.getJwtToken());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        log.info("=== refresh token ===");

        validateRequestHeader(request);

        String sRefreshToken = request.getHeader(ApplicationConstants.JwtInfo.HEADER_AUTHORIZATION);
        sRefreshToken = sRefreshToken.substring(ApplicationConstants.JwtInfo.PREFIX_BEARER_TOKEN.length()).trim();

        if (log.isDebugEnabled()) {
            log.debug("refresh token:{}", sRefreshToken);
        }

        if (StringUtils.isBlank(sRefreshToken)) {
            throw new BadCredentialsException("refresh token is blank.");
        }

        Jws<Claims> jwt = null;
        try {
            jwt = jwtBuilder.parseJwt(sRefreshToken);
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("Access token has expired.");
        } catch (JwtException e) {
            throw new BadCredentialsException("Access token is not available.");
        }

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

        String username = claims.getSubject();

        if (StringUtils.isBlank(username)) {
            log.error("username is blank");
            throw new BadCredentialsException("username is blank.");
        }
        UserDetailsService userService = SpringApplicationContextUtil.getBean(UserDetailsService.class);

        if (userService == null) {
            log.error("user details service is not configured");
            throw new InternalAuthenticationServiceException("user details service is not configured");
        }

        UserDetails userDetails = userService.loadUserByUsername(username);

        if (userDetails == null) {
            log.error("such user {} doesnt exist", username);
            throw new UsernameNotFoundException("such user doesnt exist");
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null,
                userDetails.getAuthorities());
        return authToken;
    }

    protected Authentication attemptSubSystemAuthentication(HttpServletRequest request, HttpServletResponse response,
            Claims claims) {
        
        String systemCode = claims.getSubject();
        
        if(StringUtils.isBlank(systemCode)){
            log.warn("system code is blank");
            throw new BadCredentialsException("system code is blank.");
        }
        
        SubSystemInfoDataService subSystemInfoDateService = SpringApplicationContextUtil.getBean(SubSystemInfoDataService.class);
        
        SysSubSystemInfo systemInfo = subSystemInfoDateService.retrieveSysSubSystemInfoWithSystemCode(systemCode);
        
        if(systemInfo == null){
            log.error("such sub system {} is not available.", systemCode);
            throw new UsernameNotFoundException("such sub system is not available.");
        }
        
        
        SubSystemAuthenticationToken token = new SubSystemAuthenticationToken(systemCode,
                null, null, systemInfo.getAuthorities());
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

        response.addHeader(ApplicationConstants.JwtInfo.HEADER_AUTHORIZATION, ApplicationConstants.JwtInfo.PREFIX_BEARER_TOKEN + accessToken.getToken());
        response.addHeader(ApplicationConstants.JwtInfo.HEADER_AUTHORIZATION_INFO, ApplicationConstants.JwtInfo.PREFIX_BEARER_TOKEN + refreshToken.getToken());

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
        String header = request.getHeader(ApplicationConstants.JwtInfo.HEADER_AUTHORIZATION);
        if (header == null || !header.startsWith(ApplicationConstants.JwtInfo.PREFIX_BEARER_TOKEN)) {
            throw new BadCredentialsException("refresh token should provide");
        }
    }

}
