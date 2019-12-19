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

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        warnNotLoadingProdSecurityConfigurationNotice();
    }

}
