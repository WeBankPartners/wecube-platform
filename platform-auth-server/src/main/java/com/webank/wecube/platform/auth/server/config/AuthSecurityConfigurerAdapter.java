package com.webank.wecube.platform.auth.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.webank.wecube.platform.auth.server.filter.JwtSsoBasedLoginFilter;
import com.webank.wecube.platform.auth.server.filter.JwtSsoBasedSecurityContextRepository;
import com.webank.wecube.platform.auth.server.handler.Http401AuthenticationEntryPoint;
import com.webank.wecube.platform.auth.server.handler.Http403AccessDeniedHandler;

public class AuthSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
    private static final String[] AUTH_WHITELIST = { //
            "/v1/api/login", //
            "/v2/api-docs", //
            "/swagger-resources", //
            "/swagger-resources/**", //
            "/configuration/ui", //
            "/configuration/security", //
            "/swagger-ui.html", //
            "/webjars/**" //
    };

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected String[] getAuthWhiteList() {
        return AUTH_WHITELIST;
    }

    protected void configure(HttpSecurity http) throws Exception {
        http //
                .cors() //
                .and() //
                .csrf() //
                .disable() //
                .sessionManagement() //
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //
                .and() //
                .securityContext() //
                .securityContextRepository(new JwtSsoBasedSecurityContextRepository()) //
                .and() //
                .addFilterBefore(new JwtSsoBasedLoginFilter(authenticationManager()), SecurityContextPersistenceFilter.class) //
                .authorizeRequests() //
                .antMatchers(getAuthWhiteList()) //
                .permitAll() //
                .anyRequest() //
                .authenticated() //
                .and() //
                .exceptionHandling() //
                .authenticationEntryPoint(new Http401AuthenticationEntryPoint()) //
                .and() //
                .exceptionHandling() //
                .accessDeniedHandler(new Http403AccessDeniedHandler()); //

    }

    protected void warnNotLoadingProdSecurityConfigurationNotice() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n********************************************************************\t");
        sb.append("\n**********                  Notice:                       **********\t");
        sb.append("\n**********      Security NOT under production profile.    **********\t");
        sb.append("\n**********       Do not use in a production system!       **********\t");
        sb.append("\n********************************************************************\t");

        log.warn(sb.toString());
    }
}
