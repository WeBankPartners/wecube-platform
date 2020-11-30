//package com.webank.wecube.platform.core.lazyDomain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.google.common.base.Objects;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//import org.hibernate.annotations.GenericGenerator;
//
//import javax.persistence.*;
//
//@Entity
//@Table(name = "plugin_package_resource_files")
//public class LazyPluginPackageResourceFile {
//
//    @Id
//    @GeneratedValue(generator = "resourceFileGenerator")
//    @GenericGenerator(name = "resourceFileGenerator", strategy = "org.hibernate.id.UUIDGenerator")
//    private String id;
//
//    @JsonBackReference
//    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
//    @JoinColumn(name = "plugin_package_id")
//    private LazyPluginPackage pluginPackage;
//
//    @Column
//    private String packageName;
//
//    @Column String packageVersion;
//
//    @Column
//    private String source;
//
//    @Column
//    private String relatedPath;
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public LazyPluginPackage getPluginPackage() {
//        return pluginPackage;
//    }
//
//    public void setPluginPackage(LazyPluginPackage pluginPackage) {
//        this.pluginPackage = pluginPackage;
//        setPackageName(pluginPackage.getName());
//        setPackageVersion(pluginPackage.getVersion());
//    }
//
//    public String getPackageName() {
//        return packageName;
//    }
//
//    public void setPackageName(String packageName) {
//        this.packageName = packageName;
//    }
//
//    public String getPackageVersion() {
//        return packageVersion;
//    }
//
//    public void setPackageVersion(String packageVersion) {
//        this.packageVersion = packageVersion;
//    }
//
//    public String getSource() {
//        return source;
//    }
//
//    public void setSource(String source) {
//        this.source = source;
//    }
//
//    public String getRelatedPath() {
//        return relatedPath;
//    }
//
//    public void setRelatedPath(String relatedPath) {
//        this.relatedPath = relatedPath;
//    }
//
//    public LazyPluginPackageResourceFile() {
//        super();
//    }
//
//    public LazyPluginPackageResourceFile(String id, LazyPluginPackage pluginPackage, String packageName, String packageVersion, String source, String relatedPath) {
//        this.id = id;
//        this.pluginPackage = pluginPackage;
//        this.packageName = packageName;
//        this.packageVersion = packageVersion;
//        this.source = source;
//        this.relatedPath = relatedPath;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        LazyPluginPackageResourceFile that = (LazyPluginPackageResourceFile) o;
//        return Objects.equal(packageName, that.packageName) &&
//                Objects.equal(packageVersion, that.packageVersion) &&
//                Objects.equal(source, that.source) &&
//                Objects.equal(relatedPath, that.relatedPath);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hashCode(packageName, packageVersion, source, relatedPath);
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
//    }
//}
