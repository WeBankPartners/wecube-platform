package com.webank.wecube.platform.gateway.filter.factory;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.gateway.route")
public class DynamicRouteProperties {
    private String routeConfigServer;
    private String routeConfigUri;
    private String routeConfigAccessKey;

    public String getRouteConfigServer() {
        return routeConfigServer;
    }

    public void setRouteConfigServer(String routeConfigServer) {
        this.routeConfigServer = routeConfigServer;
    }

    public String getRouteConfigUri() {
        return routeConfigUri;
    }

    public void setRouteConfigUri(String routeConfigUri) {
        this.routeConfigUri = routeConfigUri;
    }

    public String getRouteConfigAccessKey() {
        return routeConfigAccessKey;
    }

    public void setRouteConfigAccessKey(String routeConfigAccessKey) {
        this.routeConfigAccessKey = routeConfigAccessKey;
    }

}
