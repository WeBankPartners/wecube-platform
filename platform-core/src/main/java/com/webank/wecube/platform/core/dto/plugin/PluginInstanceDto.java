package com.webank.wecube.platform.core.dto.plugin;

public class PluginInstanceDto {

    private String id;

    private String host;

    private String containerName;

    private Integer port;

    private String containerStatus;

    private String packageId;

    private String dockerInstanceResourceId;

    private String instanceName;

    private String pluginMysqlInstanceResourceId;

    private String s3bucketResourceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getContainerStatus() {
        return containerStatus;
    }

    public void setContainerStatus(String containerStatus) {
        this.containerStatus = containerStatus;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getDockerInstanceResourceId() {
        return dockerInstanceResourceId;
    }

    public void setDockerInstanceResourceId(String dockerInstanceResourceId) {
        this.dockerInstanceResourceId = dockerInstanceResourceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getPluginMysqlInstanceResourceId() {
        return pluginMysqlInstanceResourceId;
    }

    public void setPluginMysqlInstanceResourceId(String pluginMysqlInstanceResourceId) {
        this.pluginMysqlInstanceResourceId = pluginMysqlInstanceResourceId;
    }

    public String getS3bucketResourceId() {
        return s3bucketResourceId;
    }

    public void setS3bucketResourceId(String s3bucketResourceId) {
        this.s3bucketResourceId = s3bucketResourceId;
    }

}
