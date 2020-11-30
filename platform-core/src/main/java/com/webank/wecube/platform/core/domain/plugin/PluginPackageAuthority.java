//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//import javax.persistence.*;
//
//@Entity
//@Table(name = "plugin_package_authorities")
//public class PluginPackageAuthority {
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
//    private String roleName;
//
//    @Column
//    private String menuCode;
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
//    public String getRoleName() {
//        return roleName;
//    }
//
//    public void setRoleName(String roleName) {
//        this.roleName = roleName;
//    }
//
//    public String getMenuCode() {
//        return menuCode;
//    }
//
//    public void setMenuCode(String menuCode) {
//        this.menuCode = menuCode;
//    }
//
//    public PluginPackageAuthority() {
//        super();
//    }
//
//    public PluginPackageAuthority(String id, PluginPackage pluginPackage, String roleName, String menuCode) {
//        this.id = id;
//        this.pluginPackage = pluginPackage;
//        this.roleName = roleName;
//        this.menuCode = menuCode;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
//    }
//}
