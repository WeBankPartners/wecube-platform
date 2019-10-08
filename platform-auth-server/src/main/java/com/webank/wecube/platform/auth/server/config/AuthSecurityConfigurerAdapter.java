package com.webank.wecube.platform.auth.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.webank.wecube.platform.auth.server.filter.JwtSsoBasedAuthenticationFilter;
import com.webank.wecube.platform.auth.server.filter.JwtSsoBasedLoginFilter;
import com.webank.wecube.platform.auth.server.filter.JwtSsoBasedRefreshTokenFilter;
import com.webank.wecube.platform.auth.server.filter.JwtSsoBasedSecurityContextRepository;
import com.webank.wecube.platform.auth.server.handler.Http401AuthenticationEntryPoint;
import com.webank.wecube.platform.auth.server.handler.Http403AccessDeniedHandler;
import com.webank.wecube.platform.auth.server.handler.JwtSsoBasedAuthenticationFailureHandler;
import com.webank.wecube.platform.auth.server.service.LocalUserDetailsService;

/**
 * 
 * @author gavin
 *
 */
public class AuthSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
    private static final String[] AUTH_WHITELIST = { //
//            "/v1/api/login", //
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

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LocalUserDetailsService userDetailsService;

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
                .addFilterBefore(jwtSsoBasedLoginFilter(), SecurityContextPersistenceFilter.class) //
                .addFilterBefore(new JwtSsoBasedRefreshTokenFilter(authenticationManager()),
                        SecurityContextPersistenceFilter.class) //
                .addFilter(new JwtSsoBasedAuthenticationFilter(authenticationManager()))//
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

    protected JwtSsoBasedLoginFilter jwtSsoBasedLoginFilter() throws Exception {
        JwtSsoBasedLoginFilter f = new JwtSsoBasedLoginFilter(authenticationManager());
        f.setAuthenticationFailureHandler(new JwtSsoBasedAuthenticationFailureHandler());

        return f;
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

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ErrorPageFilter errorPageFilter() {
        return new ErrorPageFilter();
    }

    @Bean
    public FilterRegistrationBean<ErrorPageFilter> disableSpringBootErrorFilter(ErrorPageFilter filter) {
        FilterRegistrationBean<ErrorPageFilter> filterRegistrationBean = new FilterRegistrationBean<ErrorPageFilter>();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }
}
