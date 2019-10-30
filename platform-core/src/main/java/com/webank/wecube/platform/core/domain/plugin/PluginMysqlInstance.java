package com.webank.wecube.platform.core.domain.plugin;

import javax.persistence.*;

@Entity
@Table(name = "plugin_mysql_instances")
public class PluginMysqlInstance {

    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private String schemaName;

    @ManyToOne
    @JoinColumn(name = "plugun_package_id")
    private PluginPackage pluginPackage;

    @Column
    private Integer resourceItemId;
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private String status;

    public PluginMysqlInstance() {
    }

    public PluginMysqlInstance(String schemaName, Integer resourceItemId, String username, String password,
            String status, PluginPackage pluginPackage) {
        this.schemaName = schemaName;
        this.resourceItemId = resourceItemId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.pluginPackage = pluginPackage;
    }

    public Integer getId() {
        return id;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public Integer getResourceItemId() {
        return resourceItemId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setResourceItemId(Integer resourceItemId) {
        this.resourceItemId = resourceItemId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(PluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
    }

}
