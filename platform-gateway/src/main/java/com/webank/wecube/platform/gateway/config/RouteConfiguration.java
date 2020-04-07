package com.webank.wecube.platform.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.webank.wecube.platform.gateway.filter.factory.DynamicRouteGatewayFilterFactory;
import com.webank.wecube.platform.gateway.filter.factory.DynamicRouteProperties;
import com.webank.wecube.platform.gateway.filter.factory.ExRetryGatewayFilterFactory;
import com.webank.wecube.platform.gateway.filter.factory.JwtSsoTokenGatewayFilterFactory;

@Configuration
@EnableConfigurationProperties({
    DynamicRouteProperties.class
})
public class RouteConfiguration {
    @Autowired
    private DynamicRouteProperties dynamicRouteProperties;
    
    @Bean
    public JwtSsoTokenGatewayFilterFactory jwtSsoTokenGatewayFilterFactory(){
        return new JwtSsoTokenGatewayFilterFactory();
    }
    
    @Bean
    public DynamicRouteGatewayFilterFactory dynamicRouteGatewayFilterFactory(){
        DynamicRouteGatewayFilterFactory f =  new DynamicRouteGatewayFilterFactory();
        f.setDynamicRouteProperties(dynamicRouteProperties);
        
        return f;
    }
    
    @Bean
    public ExRetryGatewayFilterFactory exRetryGatewayFilterFactory(){
        return new ExRetryGatewayFilterFactory();
    }
}
