package com.webank.wecube.platform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.webank.wecube.platform.gateway.filter.factory.DynamicRouteGatewayFilterFactory;
import com.webank.wecube.platform.gateway.filter.factory.JwtSsoTokenGatewayFilterFactory;

@Configuration
public class RouteConfiguration {
    
    @Bean
    public JwtSsoTokenGatewayFilterFactory jwtSsoTokenGatewayFilterFactory(){
        return new JwtSsoTokenGatewayFilterFactory();
    }
    
    @Bean
    public DynamicRouteGatewayFilterFactory dynamicRouteGatewayFilterFactory(){
        return new DynamicRouteGatewayFilterFactory();
    }
}
