package com.webank.wecube.platform.core.support.gateway;

public class RouteItem {
    private String name;
    private String schema;
    private String host;
    private String port;

    public RouteItem() {
    }

    public RouteItem(String name, String schema, String host, String port) {
        this.name = name;
        this.schema = schema;
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
