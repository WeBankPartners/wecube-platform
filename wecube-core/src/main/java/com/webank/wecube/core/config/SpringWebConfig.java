package com.webank.wecube.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.webank.wecube.core.commons.CustomRolesPrefixPostProcessor;
import com.webank.wecube.core.commons.ApplicationProperties;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.webank.wecube.core.interceptor.WebUsernameInterceptor;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

@Configuration
@EnableWebMvc
@EnableSwagger2
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@ComponentScan({"com.webank.wecube.core.controller"})
public class SpringWebConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private WebUsernameInterceptor webUserInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/");
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webUserInterceptor).addPathPatterns("/**");
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (applicationProperties.isSecurityEnabled()) {
            http.exceptionHandling()
                    .authenticationEntryPoint(casAuthenticationEntryPoint())
                    .and()
                    .addFilter(casAuthenticationFilter())
                    .addFilterBefore(logoutFilter(), LogoutFilter.class)
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .logout().permitAll()
                    .and()
                    .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        } else {
            http.csrf().disable().antMatcher("/**").anonymous();
        }
    }


    public AuthenticationEntryPoint casAuthenticationEntryPoint() {
        CasAuthenticationEntryPoint point = new CasAuthenticationEntryPoint();
        point.setLoginUrl(applicationProperties.getCasServerUrl() + "/login");
        point.setServiceProperties(serviceProperties());
        return point;
    }

    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    public LogoutFilter logoutFilter() {
        return new LogoutFilter(applicationProperties.getCasServerUrl() + "/logout?service=" + getServerUrl(), new SecurityContextLogoutHandler());
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setTicketValidator(new Cas20ServiceTicketValidator(applicationProperties.getCasServerUrl()));
        provider.setServiceProperties(serviceProperties());
        provider.setKey("casAuthProviderKey");
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public static CustomRolesPrefixPostProcessor customRolesPrefixPostProcessor() {
        return new CustomRolesPrefixPostProcessor();
    }

    private ServiceProperties serviceProperties() {
        ServiceProperties properties = new ServiceProperties();
        properties.setService(getServerUrl() + "/login/cas");
        properties.setSendRenew(false);
        return properties;
    }

    private String getServerUrl() {
        return String.format("http://%s:%d", serverProperties.getAddress().getHostAddress(), serverProperties.getPort());
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jacksonMessageConverter());
    }

    private MappingJackson2HttpMessageConverter jacksonMessageConverter() {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Hibernate5Module());

        messageConverter.setObjectMapper(mapper);
        return messageConverter;
    }
}
