package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "plugin_instances")
public class PluginInstance {
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_REMOVED = "REMOVED";

    @Id
    @GeneratedValue
    private Integer id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "package_id")
    private PluginPackage pluginPackage;

    @JsonManagedReference
    @OneToMany(mappedBy = "mysql_instance_resource_id", fetch = FetchType.EAGER)
    private Set<PluginMysqlInstance> pluginMysqlInstance = new LinkedHashSet<>();

    @Column
    private Integer s3BucketResourceId;
    @Column
    private String instanceContainerId;
    @Column
    private String host;
    @Column
    private Integer port;
    @Column
    private String status;

    public PluginInstance() {
    }

    public PluginInstance(Integer id, PluginPackage pluginPackage, String instanceContainerId, String host,
            Integer port, String status) {
        this.id = id;
        this.pluginPackage = pluginPackage;
        this.instanceContainerId = instanceContainerId;
        this.host = host;
        this.port = port;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(PluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
    }

    public String getInstanceContainerId() {
        return instanceContainerId;
    }

    public void setInstanceContainerId(String instanceContainerId) {
        this.instanceContainerId = instanceContainerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static String getStatusRunning() {
        return STATUS_RUNNING;
    }

    public static String getStatusRemoved() {
        return STATUS_REMOVED;
    }

    public Set<PluginMysqlInstance> getPluginMysqlInstance() {
        return pluginMysqlInstance;
    }

    public Integer getS3BucketResourceId() {
        return s3BucketResourceId;
    }

    public void setPluginMysqlInstance(Set<PluginMysqlInstance> pluginMysqlInstance) {
        this.pluginMysqlInstance = pluginMysqlInstance;
    }

    public void setS3BucketResourceId(Integer s3BucketResourceId) {
        this.s3BucketResourceId = s3BucketResourceId;
    }

}
