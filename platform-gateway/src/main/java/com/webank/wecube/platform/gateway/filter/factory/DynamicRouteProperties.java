package com.webank.wecube.platform.gateway.filter.factory;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.gateway.route")
public class DynamicRouteProperties {
    private String routeConfigServer;
    private String routeConfigUri;
    private String routeConfigAccessKey;

    private boolean enableRetry = true;
    private int retryIntervalOfSeconds = 5;
    private int refreshIntervalOfMinutes = 10;

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

    public boolean isEnableRetry() {
        return enableRetry;
    }

    public void setEnableRetry(boolean enableRetry) {
        this.enableRetry = enableRetry;
    }

    public int getRetryIntervalOfSeconds() {
        return retryIntervalOfSeconds;
    }

    public void setRetryIntervalOfSeconds(int retryIntervalOfSeconds) {
        this.retryIntervalOfSeconds = retryIntervalOfSeconds;
    }

    public int getRefreshIntervalOfMinutes() {
        return refreshIntervalOfMinutes;
    }

    public void setRefreshIntervalOfMinutes(int refreshIntervalOfMinutes) {
        this.refreshIntervalOfMinutes = refreshIntervalOfMinutes;
    }

}
