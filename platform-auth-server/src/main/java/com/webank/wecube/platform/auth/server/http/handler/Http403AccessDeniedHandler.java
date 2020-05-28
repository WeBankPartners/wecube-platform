package com.webank.wecube.platform.auth.server.http.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;

/**
 * 
 * @author gavin
 *
 */
public class Http403AccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(Http403AccessDeniedHandler.class);
    private String headerValue = "Fully authorization is required.";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("=== access denied ===");

        response.setHeader(ApplicationConstants.JwtInfo.HEADER_AUTHORIZATION, this.headerValue);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());

    }

}
