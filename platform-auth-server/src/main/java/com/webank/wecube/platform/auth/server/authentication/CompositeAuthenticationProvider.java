package com.webank.wecube.platform.auth.server.authentication;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.auth.server.model.AuthContext;
import com.webank.wecube.platform.auth.server.model.SysUser;
import com.webank.wecube.platform.auth.server.service.LocalUserDetailsService;

@Component("compositeAuthenticationProvider")
public class CompositeAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(CompositeAuthenticationProvider.class);

    @Autowired
    private LocalUserDetailsService localUserDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UmAuthenticationChecker umAuthenticationChecker;
    
    public CompositeAuthenticationProvider(LocalUserDetailsService localUserDetailsService,
            PasswordEncoder passwordEncoder) {
        super();
        this.localUserDetailsService = localUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication == null) {
            throw new BadCredentialsException("Bad credential:none authentication provided.");
        }

        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            throw new IllegalArgumentException("such authentication type doesnt supported");
        }

        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;

        String username = authToken.getName();
        if (username == null || username.trim().length() < 1) {
            log.debug("blank user name");
            throw new BadCredentialsException("Bad credential:blank username.");
        }

        return doAuthentication(username, authToken);
    }

    protected Authentication doAuthentication(String username, UsernamePasswordAuthenticationToken authToken)
            throws AuthenticationException {
        SysUser user = null;
        try {
            UserDetails userDetails = localUserDetailsService.loadUserByUsername(username);
            if (userDetails != null && userDetails instanceof SysUser) {
                user = (SysUser) userDetails;
            }
        } catch (UsernameNotFoundException e) {
            log.debug("{} does not exist", username);
            throw e;
        }

        if (user == null) {
            log.debug("User does not exist");
            throw new BadCredentialsException("Bad credential.");
        }

        try {
            additionalAuthenticationChecks(user, authToken);
        } catch (Exception e) {
            log.info("additional authentication checking failed,{}", e.getMessage());
            throw new BadCredentialsException("Bad credential:bad password.");
        }
        Object principal = user;

        return createSuccessAuthentication(principal, authToken, user);
    }

    protected void additionalAuthenticationChecks(SysUser user, UsernamePasswordAuthenticationToken authToken) {
        String authSource = user.getAuthSource();
        if(StringUtils.isBlank(authSource)){
            authSource = AuthContext.LOCAL_AUTH_SOURCE;
        }
        
        if(AuthContext.LOCAL_AUTH_SOURCE.equalsIgnoreCase(authSource)){
            checkAuthentication(user,authToken);
            return;
        }
        
        if(AuthContext.UM_AUTH_SOURCE.equalsIgnoreCase(authSource)){
            umAuthenticationChecker.checkAuthentication(user, authToken);
            return;
        }
        
        throw new BadCredentialsException("Unknown credential type.");
    }
    
    protected void checkAuthentication(SysUser user, UsernamePasswordAuthenticationToken authToken){
        String presentedPassword = authToken.getCredentials().toString();
        if (!passwordEncoder.matches(presentedPassword, user.getPassword())){
            throw new BadCredentialsException("Bad credential:bad password.");
        }
        return;
    }

    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
            UserDetails user) {
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(principal,
                authentication.getCredentials(), user.getAuthorities());
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
   

}
