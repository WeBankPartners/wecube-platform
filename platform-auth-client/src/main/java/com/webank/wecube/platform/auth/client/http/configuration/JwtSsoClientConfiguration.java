package com.webank.wecube.platform.auth.client.http.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.auth.client.context.DefaultJwtSsoClientContext;
import com.webank.wecube.platform.auth.client.context.JwtSsoClientContext;
import com.webank.wecube.platform.auth.client.http.JwtSsoRestTemplate;

@Configuration
@EnableConfigurationProperties({ JwtSsoClientProperties.class })
public class JwtSsoClientConfiguration {

    @Autowired
    private JwtSsoClientProperties jwtSsoClientProperties;

    @Autowired
    private RestTemplate restTemplate;

    @ConditionalOnMissingBean(JwtSsoRestTemplate.class)
    @Bean
    public JwtSsoRestTemplate jwtSsoRestTemplate(JwtSsoClientContext ctx) {
        return new JwtSsoRestTemplate(ctx);
    }

    @ConditionalOnMissingBean(JwtSsoClientContext.class)
    @Bean
    public JwtSsoClientContext jwtSsoClientContext() {
        JwtSsoClientContext ctx = new DefaultJwtSsoClientContext(jwtSsoClientProperties, restTemplate);
        return ctx;
    }

    @ConditionalOnMissingBean(RestTemplate.class)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
