package com.webank.wecube.platform.core.domain.plugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.webank.wecube.platform.core.domain.ResourceItem;

@Entity
@Table(name = "plugin_mysql_instances")
public class PluginMysqlInstance {

    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private String schemaName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plugun_package_id")
    private PluginInstance pluginInstance;
    
    @Column(name="plugun_package_id",insertable=false,updatable=false)
    private int pluginPackageId;

    @Column(name = "resource_item_id")
    private Integer resourceItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_item_id",insertable=false,updatable=false)
    private ResourceItem resourceItem;
    
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private String status;

    public PluginMysqlInstance() {
    }

    public PluginMysqlInstance(String schemaName, Integer resourceItemId, String username, String password,
            String status) {
        this.schemaName = schemaName;
        this.resourceItemId = resourceItemId;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public PluginInstance getPluginInstance() {
        return pluginInstance;
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

    public void setPluginInstance(PluginInstance pluginInstance) {
        this.pluginInstance = pluginInstance;
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

    public ResourceItem getResourceItem() {
        return resourceItem;
    }

    public void setResourceItem(ResourceItem resourceItem) {
        this.resourceItem = resourceItem;
    }

    public int getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(int pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

}
