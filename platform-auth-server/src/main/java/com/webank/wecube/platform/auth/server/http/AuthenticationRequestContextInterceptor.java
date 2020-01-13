package com.webank.wecube.platform.auth.server.http;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.webank.wecube.platform.auth.server.http.AuthenticationContextHolder.AuthenticatedUser;

@Component
public class AuthenticationRequestContextInterceptor implements HandlerInterceptor {
    public static final String REQUEST_ATTR_KEY_CURRENT_USER = "REQ_ATTR_KEY_CURRENT_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            AuthenticatedUser currentUser = new AuthenticatedUser(userPrincipal.getName(),
                    request.getHeader(HttpHeaders.AUTHORIZATION), extractAuthorities(userPrincipal));

            AuthenticationContextHolder.setAuthenticatedUser(currentUser);

            request.setAttribute(REQUEST_ATTR_KEY_CURRENT_USER, currentUser);
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
        request.removeAttribute(REQUEST_ATTR_KEY_CURRENT_USER);
    }
}
