package com.webank.wecube.platform.auth.server.authentication;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.webank.wecube.platform.auth.server.model.AuthContext;

public class UmAuthenticationChecker implements AuthenticationChecker{
    private static final Logger log = LoggerFactory.getLogger(UmAuthenticationChecker.class);

    public void checkAuthentication(AuthContext ctx, Authentication authToken) {

        verifyAuthToken((UsernamePasswordAuthenticationToken)authToken);

        
    }

    private void verifyAuthToken(UsernamePasswordAuthenticationToken authToken) {
        String username = authToken.getName();
        String password = (String) authToken.getCredentials();

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BadCredentialsException("Bad credential:blank username or password.");
        }
    }
}
