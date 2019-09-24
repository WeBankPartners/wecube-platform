package com.webank.wecube.platform.auth.server.config;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.info("=== configure AuthenticationManagerBuilder ===");
        auth.inMemoryAuthentication() //
                .withUser("marissa") //
                .password("{noop}koala") //
                .roles("USER") //
                .and() //
                .withUser("paul") //
                .password("{noop}emu") //
                .roles("USER") //
                .and() //
                .withUser("umadmin") //
                .password("{noop}123456") //
                .roles("TESTER") //
        ; //
    }

    protected void configure(HttpSecurity http) throws Exception {
        log.info("==== configure HttpSecurity ====");
        super.configure(http);
        // http.addFilter(new
        // JwtSsoBasedAuthenticationFilter(authenticationManager()));
    }
}
