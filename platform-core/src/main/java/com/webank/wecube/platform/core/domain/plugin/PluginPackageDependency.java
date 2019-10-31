package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "plugin_package_dependencies")
public class PluginPackageDependency {

    @Id
    @GeneratedValue
    private int id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "plugin_package_id")
    private PluginPackage pluginPackage;

    @Column
    private String dependencyPackageName;

    @Column
    private String dependencyPackageVersion;

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
    }

    public String getDependencyPackageName() {
        return dependencyPackageName;
    }

    public void setDependencyPackageName(String dependencyPackageName) {
        this.dependencyPackageName = dependencyPackageName;
    }

    public String getDependencyPackageVersion() {
        return dependencyPackageVersion;
    }

    public void setDependencyPackageVersion(String dependencyPackageVersion) {
        this.dependencyPackageVersion = dependencyPackageVersion;
    }

    public PluginPackageDependency() {
        super();
    }

    public PluginPackageDependency(int id, PluginPackage pluginPackage, String dependencyPackageName, String dependencyPackageVersion) {
        this.id = id;
        this.pluginPackage = pluginPackage;
        this.dependencyPackageName = dependencyPackageName;
        this.dependencyPackageVersion = dependencyPackageVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPackageDependency that = (PluginPackageDependency) o;
        return id == that.id &&
                Objects.equal(dependencyPackageName, that.dependencyPackageName) &&
                Objects.equal(dependencyPackageVersion, that.dependencyPackageVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, dependencyPackageName, dependencyPackageVersion);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
    }
}
