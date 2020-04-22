package com.webank.wecube.platform.auth.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;

/**
 * 
 * @author gavin
 *
 */
@Primary
@Profile({ ApplicationConstants.Profile.PROD })
@Configuration
@EnableWebSecurity(debug = false)
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends AuthSecurityConfigurerAdapter {
	private static final String[] AUTH_WHITELIST = { //
            "/v1/health-check"
    };
	
	@Override
    protected String[] getAuthWhiteList() {
        return AUTH_WHITELIST;
    }
    
}
