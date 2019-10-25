package com.webank.wecube.platform.core.domain.plugin;

import com.webank.wecube.platform.core.domain.ResourceItem;

import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "plugin_mysql_instances")
@Setter
public class PluginMysqlInstance {

    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private String schemaName;

    @ManyToOne
    @JoinColumn(name = "plugun_instance_id")
    private PluginInstance pluginInstance;

    @Column
    @JoinColumn(name = "mysql_resource_id")
    private ResourceItem mysqlResource;
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private String status;

    public PluginMysqlInstance() {
    }

    public PluginMysqlInstance(String schemaName, ResourceItem mysqlResource, String username, String password,
            String status) {
        this.schemaName = schemaName;
        this.mysqlResource = mysqlResource;
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

    public ResourceItem getMysqlResource() {
        return mysqlResource;
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

}
