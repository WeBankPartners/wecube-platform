package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.common.base.Objects;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "plugin_package_resource_files")
public class PluginPackageResourceFile {

    @Id
    @GeneratedValue
    private int id;

    @JsonBackReference
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "plugin_package_id")
    private PluginPackage pluginPackage;

    @Column
    private String packageName;

    @Column String packageVersion;

    @Column
    private String source;

    @Column
    private String relatedPath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(PluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
        setPackageName(pluginPackage.getName());
        setPackageVersion(pluginPackage.getVersion());
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRelatedPath() {
        return relatedPath;
    }

    public void setRelatedPath(String relatedPath) {
        this.relatedPath = relatedPath;
    }

    public PluginPackageResourceFile() {
        super();
    }

    public PluginPackageResourceFile(int id, PluginPackage pluginPackage, String packageName, String packageVersion, String source, String relatedPath) {
        this.id = id;
        this.pluginPackage = pluginPackage;
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        this.source = source;
        this.relatedPath = relatedPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPackageResourceFile that = (PluginPackageResourceFile) o;
        return id == that.id &&
                Objects.equal(packageName, that.packageName) &&
                Objects.equal(packageVersion, that.packageVersion) &&
                Objects.equal(source, that.source) &&
                Objects.equal(relatedPath, that.relatedPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, packageName, packageVersion, source, relatedPath);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
    }
}
