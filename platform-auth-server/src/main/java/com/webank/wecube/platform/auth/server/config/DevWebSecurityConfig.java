package com.webank.wecube.platform.auth.server.config;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;

@Profile({ ApplicationConstants.Profile.DEV, ApplicationConstants.Profile.DEFAULT })
@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(securedEnabled = true)
public class DevWebSecurityConfig extends AuthSecurityConfigurerAdapter {
    private static final String[] AUTH_WHITELIST = { //
            // "/v1/api/login", //
//            "/v1/users/**", // for dev only
//            "/v1/roles/**", // for dev only
            "/v1/api/ping", //
            "/v2/api-docs", //
            "/error", //
            "/swagger-resources", //
            "/swagger-resources/**", //
            "/configuration/ui", //
            "/configuration/security", //
            "/swagger-ui.html", //
            "/webjars/**" //
    };

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        warnNotLoadingProdSecurityConfigurationNotice();
    }

    @Override
    protected String[] getAuthWhiteList() {
        return AUTH_WHITELIST;
    }

    
}
