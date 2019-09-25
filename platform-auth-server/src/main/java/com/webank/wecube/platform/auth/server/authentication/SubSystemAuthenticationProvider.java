package com.webank.wecube.platform.auth.server.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.auth.server.service.SubSystemInfoDataService;

@Component("subSystemAuthenticationProvider")
public class SubSystemAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(SubSystemAuthenticationProvider.class);
    
    @Autowired
    private SubSystemInfoDataService subSystemDataService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SubSystemAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
