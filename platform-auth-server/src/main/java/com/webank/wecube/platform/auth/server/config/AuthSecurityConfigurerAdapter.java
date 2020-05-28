package com.webank.wecube.platform.auth.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.webank.wecube.platform.auth.server.http.AuthenticationRequestContextInterceptor;
import com.webank.wecube.platform.auth.server.http.filter.JwtSsoBasedAuthenticationFilter;
import com.webank.wecube.platform.auth.server.http.filter.JwtSsoBasedLoginFilter;
import com.webank.wecube.platform.auth.server.http.filter.JwtSsoBasedRefreshTokenFilter;
import com.webank.wecube.platform.auth.server.http.filter.JwtSsoBasedSecurityContextRepository;
import com.webank.wecube.platform.auth.server.http.handler.Http401AuthenticationEntryPoint;
import com.webank.wecube.platform.auth.server.http.handler.Http403AccessDeniedHandler;
import com.webank.wecube.platform.auth.server.http.handler.JwtSsoBasedAuthenticationFailureHandler;
import com.webank.wecube.platform.auth.server.service.LocalUserDetailsService;

/**
 * 
 * @author gavin
 *
 */
@EnableConfigurationProperties({ AuthServerProperties.class })
public class AuthSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected LocalUserDetailsService userDetailsService;

    @Autowired
    protected AuthServerProperties authServerProperties;

    @Autowired
    protected AuthenticationRequestContextInterceptor authenticationRequestContextInterceptor;

    protected String[] getAuthWhiteList() {
        return new String[]{};
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationRequestContextInterceptor).excludePathPatterns("/v1/api/login",
                "/v1/api/token");
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
                .addFilterBefore(new JwtSsoBasedRefreshTokenFilter(authenticationManager(), authServerProperties),
                        SecurityContextPersistenceFilter.class) //
                .addFilter(new JwtSsoBasedAuthenticationFilter(authenticationManager(), authServerProperties))//
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
        JwtSsoBasedLoginFilter f = new JwtSsoBasedLoginFilter(authServerProperties);
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
    public RestTemplate restTemplate() {
        return new RestTemplate();
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
