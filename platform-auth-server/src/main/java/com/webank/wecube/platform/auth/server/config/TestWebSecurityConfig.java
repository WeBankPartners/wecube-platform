package com.webank.wecube.platform.auth.server.config;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;

@Profile({ ApplicationConstants.Profile.TEST })
@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(securedEnabled = true)
public class TestWebSecurityConfig extends AuthSecurityConfigurerAdapter {
    
    private static final String[] AUTH_WHITELIST = { //
    		"/v1/health-check",//
            "/error", //
            "/swagger-resources", //
            "/swagger-resources/**", //
            "/configuration/ui", //
            "/configuration/security", //
            "/swagger-ui.html", //
            "/webjars/**" //
    };

    @PostConstruct
    public void afterPropertiesSet() throws Exception{
        warnNotLoadingProdSecurityConfigurationNotice();
    }
    
    protected String[] getAuthWhiteList() {
        return AUTH_WHITELIST;
    }
}
