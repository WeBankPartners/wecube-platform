package com.webank.wecube.platform.auth.server.http.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoBasedAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private ObjectMapper objectMapper = new ObjectMapper();
    private String headerValue = "Bearer realm=\"Central Authentication Server\";profile=\"JWT\";";

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        logger.info("=== authentication failed ===");

        response.setHeader(ApplicationConstants.JwtInfo.HEADER_WWW_AUTHENTICATE, translateAuthenticateHeader(exception));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        CommonResponseDto responseBody = CommonResponseDto.error(exception.getMessage());
        response.getOutputStream().println(objectMapper.writeValueAsString(responseBody));
        response.getOutputStream().flush();
    }
    
    protected String translateAuthenticateHeader(AuthenticationException e){
        StringBuilder sb = new StringBuilder();
        sb.append(this.headerValue);
        sb.append("error=\"").append(e.getMessage()).append("\";");
        
        return sb.toString();
    }
}
