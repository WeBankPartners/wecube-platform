package com.webank.wecube.platform.gateway.authclient.http.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.gateway.authclient.context.DefaultJwtSsoClientContext;
import com.webank.wecube.platform.gateway.authclient.context.JwtSsoClientContext;
import com.webank.wecube.platform.gateway.authclient.http.JwtSsoRestTemplate;

/**
 * 
 * @author gavin
 *
 */
@Configuration
@EnableConfigurationProperties({ JwtSsoClientProperties.class })
public class JwtSsoClientConfiguration {

    @Autowired
    private JwtSsoClientProperties jwtSsoClientProperties;

    private RestTemplate restTemplate = new RestTemplate();

    @ConditionalOnMissingBean(JwtSsoRestTemplate.class)
    @Bean("jwtSsoRestTemplate")
    public JwtSsoRestTemplate jwtSsoRestTemplate(JwtSsoClientContext ctx) {
        return new JwtSsoRestTemplate(ctx);
    }

    @ConditionalOnMissingBean(JwtSsoClientContext.class)
    @Bean
    public JwtSsoClientContext jwtSsoClientContext() {
        JwtSsoClientContext ctx = new DefaultJwtSsoClientContext(jwtSsoClientProperties, restTemplate);
        return ctx;
    }

}
