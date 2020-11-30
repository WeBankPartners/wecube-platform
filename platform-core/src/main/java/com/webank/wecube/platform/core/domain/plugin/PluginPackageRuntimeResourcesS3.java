//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//import javax.persistence.*;
//
//@Entity
//@Table(name = "plugin_package_runtime_resources_s3")
//public class PluginPackageRuntimeResourcesS3 {
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
//    private String bucketName;
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
//    public String getBucketName() {
//        return bucketName;
//    }
//
//    public void setBucketName(String bucketName) {
//        this.bucketName = bucketName;
//    }
//
//    public PluginPackageRuntimeResourcesS3() {
//        super();
//    }
//
//    public PluginPackageRuntimeResourcesS3(String id, PluginPackage pluginPackage, String bucketName) {
//        this.id = id;
//        this.pluginPackage = pluginPackage;
//        this.bucketName = bucketName;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
//    }
//}
