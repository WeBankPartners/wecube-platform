package com.webank.wecube.platform.gateway.dto;

public class RouteItemInfoDto {
    private String name;
    private String schema;
    private String host;
    private String port;
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
    @Override
    public String toString() {
        return "name=" + name + ", schema=" + schema + ", host=" + host + ", port=" + port + "";
    }
    
    
}
