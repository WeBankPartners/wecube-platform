package com.webank.wecube.platform.auth.server.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoBasedSecurityContextRepository implements SecurityContextRepository {

    private static final Logger log = LoggerFactory.getLogger(JwtSsoBasedSecurityContextRepository.class);

    public JwtSsoBasedSecurityContextRepository() {
        if (log.isInfoEnabled()) {
            log.info("SecurityContextRepository:{} applied",
                    JwtSsoBasedSecurityContextRepository.class.getSimpleName());
        }
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return SecurityContextHolder.createEmptyContext();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return false;
    }

}
