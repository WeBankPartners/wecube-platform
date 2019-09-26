package com.webank.wecube.platform.auth.server.authentication;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.model.SysSubSystemInfo;
import com.webank.wecube.platform.auth.server.service.SubSystemInfoDataService;

@Component("subSystemAuthenticationProvider")
public class SubSystemAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(SubSystemAuthenticationProvider.class);

    @Autowired
    private SubSystemInfoDataService subSystemDataService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof SubSystemAuthenticationToken)) {
            log.error("only {} type supported", SubSystemAuthenticationToken.class.getSimpleName());
            throw new IllegalArgumentException("such authentication type doesnt supported");
        }

        SubSystemAuthenticationToken subSystemAuthToken = (SubSystemAuthenticationToken) authentication;
        // TODO must verify password
        
        String systemCode = (String) subSystemAuthToken.getPrincipal();
        
        SysSubSystemInfo subSystemInfo = retrieveSubSystemInfo(systemCode, subSystemAuthToken);

        return createSuccessAuthentication(subSystemInfo, subSystemAuthToken);
    }

    protected Authentication createSuccessAuthentication(SysSubSystemInfo retrievedSubSystemInfo,
            SubSystemAuthenticationToken authToken) {

        SubSystemAuthenticationToken returnAuthToken = new SubSystemAuthenticationToken(authToken.getPrincipal(),
                authToken.getCredentials(), authToken.getNonce(), retrievedSubSystemInfo.getAuthorities());
        
        return returnAuthToken;

    }

    protected SysSubSystemInfo retrieveSubSystemInfo(String systemCode, SubSystemAuthenticationToken authToken) {
        SysSubSystemInfo subSystemInfo = subSystemDataService.retrieveSysSubSystemInfoWithSystemCode(systemCode);

        if (subSystemInfo == null) {
            String errMsg = String.format("%s doesnt exist", systemCode);
            log.error(errMsg);
            throw new UsernameNotFoundException(errMsg);
        }

        return subSystemInfo;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SubSystemAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
