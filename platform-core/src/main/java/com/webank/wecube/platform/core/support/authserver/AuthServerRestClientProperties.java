package com.webank.wecube.platform.core.support.authserver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wecube.core.authserver")
public class AuthServerRestClientProperties {
    private String host = "localhost";
    private String httpSchema = "http";
    private int port = 80;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHttpSchema() {
        return httpSchema;
    }

    public void setHttpSchema(String httpSchema) {
        this.httpSchema = httpSchema;
    }
}
