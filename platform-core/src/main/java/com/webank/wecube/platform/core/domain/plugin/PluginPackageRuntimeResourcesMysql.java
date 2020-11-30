//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//import javax.persistence.*;
//
//@Entity
//@Table(name = "plugin_package_runtime_resources_mysql")
//public class PluginPackageRuntimeResourcesMysql {
//
//    @Id
//    private String id;
//
//    @JsonBackReference
//    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
//    @JoinColumn(name = "plugin_package_id")
//    private PluginPackage pluginPackage;
//
//    @Column
//    private String schemaName;
//
//    @Column
//    private String initFileName;
//
//    @Column
//    private String upgradeFileName;
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    @PrePersist
//    public void initId() {
//        this.id = DomainIdBuilder.buildDomainId(this);
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
//    public String getSchemaName() {
//        return schemaName;
//    }
//
//    public void setSchemaName(String schemaName) {
//        this.schemaName = schemaName;
//    }
//
//    public String getInitFileName() {
//        return initFileName;
//    }
//
//    public void setInitFileName(String initFileName) {
//        this.initFileName = initFileName;
//    }
//
//    public String getUpgradeFileName() {
//        return upgradeFileName;
//    }
//
//    public void setUpgradeFileName(String upgradeFileName) {
//        this.upgradeFileName = upgradeFileName;
//    }
//
//    public PluginPackageRuntimeResourcesMysql() {
//        super();
//    }
//
//    public PluginPackageRuntimeResourcesMysql(String id, PluginPackage pluginPackage, String schemaName, String initFileName, String upgradeFileName) {
//        this.id = id;
//        this.pluginPackage = pluginPackage;
//        this.schemaName = schemaName;
//        this.initFileName = initFileName;
//        this.upgradeFileName = upgradeFileName;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
//    }
//}
