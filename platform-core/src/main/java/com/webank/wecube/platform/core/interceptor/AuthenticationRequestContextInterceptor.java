package com.webank.wecube.platform.core.interceptor;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;

@Component
public class AuthenticationRequestContextInterceptor implements HandlerInterceptor {
    public static final String REQ_ATTR_KEY_CURRENT_USER = "REQ_ATTR_KEY_CURRENT_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {

            AuthenticatedUser currentUser = new AuthenticatedUser(userPrincipal.getName());
            AuthenticationContextHolder.setAuthenticatedUser(currentUser);

            request.setAttribute(REQ_ATTR_KEY_CURRENT_USER, currentUser);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthenticationContextHolder.clearCurrentUser();
        request.removeAttribute(REQ_ATTR_KEY_CURRENT_USER);
    }
}
