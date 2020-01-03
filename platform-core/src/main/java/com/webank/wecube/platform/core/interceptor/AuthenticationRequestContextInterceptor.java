package com.webank.wecube.platform.core.interceptor;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

@Component
public class AuthenticationRequestContextInterceptor implements HandlerInterceptor {
    private static final String AUTHORIZATION = "Authorization";
    public static final String REQ_ATTR_KEY_CURRENT_USER = "REQ_ATTR_KEY_CURRENT_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            AuthenticatedUser currentUser = new AuthenticatedUser(userPrincipal.getName());

            currentUser.withAuthorities(extractAuthorities(userPrincipal));
            currentUser.setToken(request.getHeader(AUTHORIZATION));
            AuthenticationContextHolder.setAuthenticatedUser(currentUser);

            request.setAttribute(REQ_ATTR_KEY_CURRENT_USER, currentUser);
        }
        return true;
    }

    private String[] extractAuthorities(Principal userPrincipal) {
        String[] authorities = new String[0];
        if (userPrincipal instanceof UsernamePasswordAuthenticationToken) {
            authorities = ((UsernamePasswordAuthenticationToken) userPrincipal).getAuthorities()
                    .stream()
                    .map(GrantedAuthority::toString)
                    .toArray(String[]::new);
        }
        return authorities;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthenticationContextHolder.clearCurrentUser();
        request.removeAttribute(REQ_ATTR_KEY_CURRENT_USER);
    }
}
