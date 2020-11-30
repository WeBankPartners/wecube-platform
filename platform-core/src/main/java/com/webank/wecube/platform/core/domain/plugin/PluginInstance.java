//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//
//import javax.persistence.*;
//
//@Entity
//@EntityListeners(value = {PluginPackageMenuStatusListener.class})
//@Table(name = "plugin_instances")
//public class PluginInstance {
//    public static final String CONTAINER_STATUS_RUNNING = "RUNNING";
//    public static final String CONTAINER_STATUS_REMOVED = "REMOVED";
//
//    @Id
//    private String id;
//
//    @JsonBackReference
//    @ManyToOne
//    @JoinColumn(name = "package_id")
//    private PluginPackage pluginPackage;
//
//    @Column
//    private String instanceName;
//
//    @Column
//    private String containerName;
//
//    @Column
//    private String host;
//
//    @Column
//    private Integer port;
//
//    @Column
//    private String pluginMysqlInstanceResourceId;
//
//    @Column(name = "s3bucket_resource_id")
//    private String s3BucketResourceId;
//
//    @Column
//    private String dockerInstanceResourceId;
//
//    @Column
//    private String containerStatus;
//
//    public PluginInstance() {
//    }
//
//    public PluginInstance(String id, PluginPackage pluginPackage, String instanceName, String host, Integer port,
//                          String containerStatus) {
//        this.id = id;
//        this.pluginPackage = pluginPackage;
//        this.instanceName = instanceName;
//        this.host = host;
//        this.port = port;
//        this.containerStatus = containerStatus;
//    }
//
//    public PluginInstance(String id, PluginPackage pluginPackage, String instanceName, String containerName, String host, Integer port) {
//        this.id = id;
//        this.pluginPackage = pluginPackage;
//        this.instanceName = instanceName;
//        this.containerName = containerName;
//        this.host = host;
//        this.port = port;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    @PrePersist
//    public void initId() {
//        this.id = DomainIdBuilder.buildDomainId(this);
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public PluginPackage getPluginPackage() {
//        return pluginPackage;
//    }
//
//    public void setPluginPackage(PluginPackage pluginPackage) {
//        this.pluginPackage = pluginPackage;
//    }
//
//    public String getContainerStatus() {
//        return containerStatus;
//    }
//
//    public void setContainerStatus(String containerStatus) {
//        this.containerStatus = containerStatus;
//    }
//
//    public String getS3BucketResourceId() {
//        return s3BucketResourceId;
//    }
//
//    public void setS3BucketResourceId(String s3BucketResourceId) {
//        this.s3BucketResourceId = s3BucketResourceId;
//    }
//
//    public String getInstanceName() {
//        return instanceName;
//    }
//
//    public void setInstanceName(String instanceName) {
//        this.instanceName = instanceName;
//    }
//
//    public String getHost() {
//        return host;
//    }
//
//    public void setHost(String host) {
//        this.host = host;
//    }
//
//    public Integer getPort() {
//        return port;
//    }
//
//    public void setPort(Integer port) {
//        this.port = port;
//    }
//
//    public String getDockerInstanceResourceId() {
//        return dockerInstanceResourceId;
//    }
//
//    public void setDockerInstanceResourceId(String dockerInstanceResourceId) {
//        this.dockerInstanceResourceId = dockerInstanceResourceId;
//    }
//
//    public String getPluginMysqlInstanceResourceId() {
//        return pluginMysqlInstanceResourceId;
//    }
//
//    public void setPluginMysqlInstanceResourceId(String pluginMysqlInstanceResourceId) {
//        this.pluginMysqlInstanceResourceId = pluginMysqlInstanceResourceId;
//    }
//
//    public String getContainerName() {
//        return containerName;
//    }
//
//    public void setContainerName(String containerName) {
//        this.containerName = containerName;
//    }
//
//}
