package com.webank.wecube.platform.core.service.dme;

public class EntityDescription {
    private String packageName;
    private String entityName;
    private String httpSchema = "http";
    private String httpHost;
    private String httpPort;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getHttpSchema() {
        return httpSchema;
    }

    public void setHttpSchema(String httpSchema) {
        this.httpSchema = httpSchema;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[packageName=");
        builder.append(packageName);
        builder.append(", entityName=");
        builder.append(entityName);
        builder.append(", httpSchema=");
        builder.append(httpSchema);
        builder.append(", httpHost=");
        builder.append(httpHost);
        builder.append(", httpPort=");
        builder.append(httpPort);
        builder.append("]");
        return builder.toString();
    }

}
