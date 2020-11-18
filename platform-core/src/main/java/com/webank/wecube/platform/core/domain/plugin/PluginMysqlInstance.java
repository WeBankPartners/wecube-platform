//package com.webank.wecube.platform.core.domain.plugin;
//
//import java.util.Date;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//import javax.persistence.PrePersist;
//import javax.persistence.Table;
//
//import com.webank.wecube.platform.core.domain.ResourceItem;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//
//@Entity
//@Table(name = "plugin_mysql_instances")
//public class PluginMysqlInstance {
//    public static final String MYSQL_INSTANCE_STATUS_ACTIVE = "active";
//    public static final String MYSQL_INSTANCE_STATUS_INACTIVE = "inactive";
//
//    @Id
//    private String id;
//    @Column
//    private String schemaName;
//
//    @ManyToOne
//    @JoinColumn(name = "plugun_package_id")
//    private PluginPackage pluginPackage;
//
//    @Column(name = "plugun_package_id", insertable = false, updatable = false)
//    private String pluginPackageId;
//
//    @Column(name = "resource_item_id")
//    private String resourceItemId;
//
//    @ManyToOne
//    @JoinColumn(name = "resource_item_id", insertable = false, updatable = false)
//    private ResourceItem resourceItem;
//
//    @Column
//    private String username;
//    @Column
//    private String password;
//    @Column
//    private String status;
//
//    @Column(name = "pre_version")
//    private String latestUpgradeVersion;
//
//    @Column(name = "created_time")
//    private Date createdTime;
//
//    @Column(name = "updated_time")
//    private Date updatedTime;
//
//    public PluginMysqlInstance() {
//    }
//
//    public PluginMysqlInstance(String schemaName, String resourceItemId, String username, String password,
//            String status, PluginPackage pluginPackage) {
//        this.schemaName = schemaName;
//        this.resourceItemId = resourceItemId;
//        this.username = username;
//        this.password = password;
//        this.status = status;
//        this.pluginPackage = pluginPackage;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    @PrePersist
//    public void initId() {
//        if (null == this.id || this.id.trim().equals("")) {
//            this.id = DomainIdBuilder.buildDomainId(null != pluginPackage ? pluginPackage.getId() : null, schemaName,
//                    username);
//        }
//    }
//
//    public String getSchemaName() {
//        return schemaName;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public String getResourceItemId() {
//        return resourceItemId;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public void setSchemaName(String schemaName) {
//        this.schemaName = schemaName;
//    }
//
//    public void setResourceItemId(String resourceItemId) {
//        this.resourceItemId = resourceItemId;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public ResourceItem getResourceItem() {
//        return resourceItem;
//    }
//
//    public void setResourceItem(ResourceItem resourceItem) {
//        this.resourceItem = resourceItem;
//    }
//
//    public String getPluginPackageId() {
//        return pluginPackageId;
//    }
//
//    public void setPluginPackageId(String pluginPackageId) {
//        this.pluginPackageId = pluginPackageId;
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
//    public String getLatestUpgradeVersion() {
//        return latestUpgradeVersion;
//    }
//
//    public void setLatestUpgradeVersion(String latestUpgradeVersion) {
//        this.latestUpgradeVersion = latestUpgradeVersion;
//    }
//
//    public Date getCreatedTime() {
//        return createdTime;
//    }
//
//    public void setCreatedTime(Date createdTime) {
//        this.createdTime = createdTime;
//    }
//
//    public Date getUpdatedTime() {
//        return updatedTime;
//    }
//
//    public void setUpdatedTime(Date updatedTime) {
//        this.updatedTime = updatedTime;
//    }
//
//}
