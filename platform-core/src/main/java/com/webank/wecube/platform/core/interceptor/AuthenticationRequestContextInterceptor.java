package com.webank.wecube.platform.core.interceptor;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        if (userPrincipal != null && (userPrincipal instanceof Authentication)) {
            Authentication auth = (Authentication)userPrincipal;
            String authToken = (String) auth.getCredentials();
            AuthenticatedUser currentUser = new AuthenticatedUser(auth.getName(),
                    authToken, extractAuthorities(userPrincipal));
            
            AuthenticationContextHolder.setAuthenticatedUser(currentUser);

            request.setAttribute(REQ_ATTR_KEY_CURRENT_USER, currentUser);
        }
        return true;
    }

    private List<String> extractAuthorities(Principal userPrincipal) {
        List<String> authorities = new ArrayList<>();
        if (userPrincipal instanceof UsernamePasswordAuthenticationToken) {
            ((UsernamePasswordAuthenticationToken) userPrincipal).getAuthorities().forEach(a -> {
                authorities.add(a.getAuthority());
            });
        }
        return authorities;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        AuthenticationContextHolder.clearCurrentUser();
        request.removeAttribute(REQ_ATTR_KEY_CURRENT_USER);
    }
}
