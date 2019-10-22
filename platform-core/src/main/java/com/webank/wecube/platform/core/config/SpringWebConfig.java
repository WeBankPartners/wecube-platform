package com.webank.wecube.platform.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.webank.wecube.platform.core.commons.ApplicationProperties;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableWebMvc
@EnableSwagger2
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@ComponentScan({"com.webank.wecube.platform.core.controller"})
public class SpringWebConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private ApplicationProperties applicationProperties;

//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    private WebUsernameInterceptor webUserInterceptor;

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

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(webUserInterceptor).addPathPatterns("/**");
//        WebMvcConfigurer.super.addInterceptors(registry);
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureLocalAuthentication(http);
//        if (applicationProperties.isSecurityEnabled()) {
//            if (applicationProperties.isAuthenticationProviderLocal()) {
//                configureLocalAuthentication(http);
//            }
//            else if (applicationProperties.isAuthenticationProviderCAS()) {
//                configureCasAuthentication(http);
//            }
//            else {
//                throw new WecubeCoreException("Unsupported authentication-provider: " + applicationProperties.getAuthenticationProvider());
//            }
//        } else {
//            http.csrf().disable().authorizeRequests().anyRequest().permitAll();
//        }
    }
    
    protected void configureLocalAuthentication(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
                .antMatchers("/login*").permitAll()
                .antMatchers("/logout*").permitAll()
//                .anyRequest().authenticated()
                .anyRequest().permitAll()
            .and()
                .formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.html")
                .failureUrl("/login.html?error=true")
            .and()
                .logout()
                .logoutUrl("/logout")
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login.html");
    }
    
//    @Override
//    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
//        if (applicationProperties.isAuthenticationProviderLocal()) {
//            auth.userDetailsService(userDetailsService).passwordEncoder(new BypassPasswordEncoder());
//        } else {
//            super.configure(auth);
//        }
//    }
    
//    protected void configureCasAuthentication(HttpSecurity http) throws Exception {
//        http.exceptionHandling()
////            .authenticationEntryPoint(casAuthenticationEntryPoint())
//            .and()
////            .addFilter(casAuthenticationFilter())
////            .addFilterBefore(casLogoutFilter(), LogoutFilter.class)
//            .authorizeRequests()
//            .anyRequest().authenticated()
//            .and()
//            .logout().permitAll()
//            .and()
//            .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
//    }


//    public AuthenticationEntryPoint casAuthenticationEntryPoint() {
//        CasAuthenticationEntryPoint point = new CasAuthenticationEntryPoint();
//        point.setLoginUrl(applicationProperties.getCasServerUrl() + "/login");
//        point.setServiceProperties(serviceProperties());
//        return point;
//    }

//    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
//        CasAuthenticationFilter filter = new CasAuthenticationFilter();
//        filter.setAuthenticationManager(authenticationManager());
//        return filter;
//    }

//    public LogoutFilter casLogoutFilter() {
//        return new LogoutFilter(applicationProperties.getCasServerUrl() + "/logout?service=" + getServerUrl(), new SecurityContextLogoutHandler());
//    }

//    @Bean
//    public CasAuthenticationProvider casAuthenticationProvider() {
//        CasAuthenticationProvider provider = new CasAuthenticationProvider();
//        provider.setTicketValidator(new Cas20ServiceTicketValidator(applicationProperties.getCasServerUrl()));
//        provider.setServiceProperties(serviceProperties());
//        provider.setKey("casAuthProviderKey");
//        provider.setUserDetailsService(userDetailsService);
//        return provider;
//    }

//    @Bean
//    public static CustomRolesPrefixPostProcessor customRolesPrefixPostProcessor() {
//        return new CustomRolesPrefixPostProcessor();
//    }

//    private ServiceProperties serviceProperties() {
//        ServiceProperties properties = new ServiceProperties();
//        properties.setService(getServerUrl() + "/login/cas");
//        properties.setSendRenew(false);
//        return properties;
//    }
//
//    private String getServerUrl() {
//        return String.format("http://%s", applicationProperties.getCasRedirectAppAddr());
//    }

//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        converters.add(jacksonMessageConverter());
//    }
//
//    private MappingJackson2HttpMessageConverter jacksonMessageConverter() {
//        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new Hibernate5Module());
//
//        messageConverter.setObjectMapper(mapper);
//        return messageConverter;
//    }
    
    
//    private class BypassPasswordEncoder implements PasswordEncoder{
//        @Override
//        public boolean matches(CharSequence rawPassword, String encodedPassword) {
//            return true;
//        }
//        
//        @Override
//        public String encode(CharSequence rawPassword) {
//            return String.valueOf(rawPassword);
//        }
//    }
    
    
}
