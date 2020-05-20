package com.webank.wecube.platform.auth.server.http.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;

/**
 * 
 * @author gavin
 *
 */
public class Http401AuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger log = LoggerFactory.getLogger(Http401AuthenticationEntryPoint.class);

    private String headerValue = "Bearer realm=\"Central Authentication Server\";profile=\"JWT\";";
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        log.info("=== authentication failed === ");
        response.setHeader(ApplicationConstants.JwtInfo.HEADER_WWW_AUTHENTICATE, translateAuthenticateHeader(authException));
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        
        CommonResponseDto responseBody = CommonResponseDto.error(authException.getMessage());
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
