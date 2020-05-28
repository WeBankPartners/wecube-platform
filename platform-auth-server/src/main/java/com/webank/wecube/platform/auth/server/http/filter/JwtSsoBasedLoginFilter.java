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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.server.authentication.CompositeAuthenticationProvider;
import com.webank.wecube.platform.auth.server.authentication.SubSystemAuthenticationProvider;
import com.webank.wecube.platform.auth.server.authentication.SubSystemAuthenticationToken;
import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.config.AuthServerProperties;
import com.webank.wecube.platform.auth.server.config.SpringApplicationContextUtil;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;
import com.webank.wecube.platform.auth.server.dto.CredentialDto;
import com.webank.wecube.platform.auth.server.dto.JwtTokenDto;
import com.webank.wecube.platform.auth.server.model.JwtToken;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoBasedLoginFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtSsoBasedLoginFilter.class);

    private static final String URI_LOGIN = "/v1/api/login";

    private final AuthServerProperties authServerProperties;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final JwtBuilder jwtBuilder;

    public JwtSsoBasedLoginFilter(AuthServerProperties authServerProperties) {
        super(new AntPathRequestMatcher(URI_LOGIN, "POST"));
        
        this.authServerProperties = authServerProperties;

        if (log.isDebugEnabled()) {
            log.debug("Filter: {} applied", JwtSsoBasedLoginFilter.class.getSimpleName());
        }
        
        jwtBuilder = new DefaultJwtBuilder(this.authServerProperties.getJwtToken());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        try {
            CredentialDto credential = objectMapper.readValue(request.getInputStream(), CredentialDto.class);

            if (log.isDebugEnabled()) {
                log.debug("LOGIN:{}", credential);
            }

            validateCredential(credential);

            if (StringUtils.isNotBlank(credential.getClientType()) && StringUtils.isNotBlank(credential.getNonce())
                    && isSubSystemClient(credential)) {
                return attemptSubSystemAuthentication(request, response, credential);
            } else {
                return attemptUserAuthentication(request, response, credential);
            }

        } catch (IOException e) {
            log.info("errors while reading credential:{}", e.getMessage(), e);
            throw new BadCredentialsException("Bad credentials.");
        }

    }

    protected void validateCredential(CredentialDto c) {
        if (c == null) {
            throw new BadCredentialsException("credentials is empty.");
        }

        if (StringUtils.isBlank(c.getUsername()) || StringUtils.isBlank(c.getPassword())) {
            throw new BadCredentialsException("credentials is blank.");
        }
    }

    protected boolean isSubSystemClient(CredentialDto credential) {
        if (ApplicationConstants.ClientType.SUB_SYSTEM.equals(credential.getClientType())) {
            return true;
        }

        return false;
    }

    protected Authentication attemptSubSystemAuthentication(HttpServletRequest request, HttpServletResponse response,
            CredentialDto credential) {
        SubSystemAuthenticationToken token = new SubSystemAuthenticationToken(credential.getUsername(),
                credential.getPassword(), credential.getNonce());
        return SpringApplicationContextUtil
                .getBean("subSystemAuthenticationProvider", SubSystemAuthenticationProvider.class).authenticate(token);
    }

    protected Authentication attemptUserAuthentication(HttpServletRequest request, HttpServletResponse response,
            CredentialDto credential) {
        final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                credential.getUsername(), credential.getPassword());

        return SpringApplicationContextUtil
                .getBean("compositeAuthenticationProvider", CompositeAuthenticationProvider.class).authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        JwtTokenDto refreshToken = jwtTokenDto(jwtBuilder.buildRefreshToken(authResult));
        JwtTokenDto accessToken = jwtTokenDto(jwtBuilder.buildAccessToken(authResult));

        response.addHeader(ApplicationConstants.JwtInfo.HEADER_AUTHORIZATION,
                ApplicationConstants.JwtInfo.PREFIX_BEARER_TOKEN + accessToken.getToken());
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

}
