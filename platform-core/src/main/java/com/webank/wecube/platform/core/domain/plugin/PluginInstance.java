package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "plugin_instances")
@Setter
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

    public PluginInstance(Integer id, PluginPackage pluginPackage, String instanceContainerId, String host, Integer port, String status) {
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
}
