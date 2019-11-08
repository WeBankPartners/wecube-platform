package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.base.Objects;
import com.webank.wecube.platform.core.domain.SystemVariable;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "plugin_packages")
public class PluginPackage {
    public enum Status {
        UNREGISTERED, REGISTERED, RUNNING, STOPPED, DECOMMISSIONED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column
    private Timestamp uploadTimestamp;

    @Column
    private boolean uiPackageIncluded;

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginPackageDependency> pluginPackageDependencies = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginPackageMenu> pluginPackageMenus = new LinkedHashSet<>();

    @JsonManagedReference
    @Transient
    private Set<PluginPackageEntity> pluginPackageEntities = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<SystemVariable> systemVariables = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginPackageAuthority> pluginPackageAuthorities = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginPackageRuntimeResourcesDocker> pluginPackageRuntimeResourcesDocker = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginPackageRuntimeResourcesMysql> pluginPackageRuntimeResourcesMysql = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginPackageRuntimeResourcesS3> pluginPackageRuntimeResourcesS3 = new LinkedHashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackage", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginConfig> pluginConfigs = new LinkedHashSet<>();

    @JsonManagedReference
    @Transient
    private Set<PluginPackageResourceFile> pluginPackageResourceFiles = new LinkedHashSet<>();

    public void addPluginConfig(PluginConfig pluginConfig) {
        this.pluginConfigs.add(pluginConfig);
    }

    public PluginPackage() {
    }

    public PluginPackage(Integer id, String name, String version, Status status, Timestamp uploadTimestamp, boolean uiPackageIncluded) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.status = status;
        this.uploadTimestamp = uploadTimestamp;
        this.uiPackageIncluded = uiPackageIncluded;
    }

    public PluginPackage(Integer id, String name, String version, Status status, Timestamp uploadTimestamp, boolean uiPackageIncluded, Set<PluginPackageDependency> pluginPackageDependencies, Set<PluginPackageMenu> pluginPackageMenus, Set<PluginPackageEntity> pluginPackageEntities, Set<SystemVariable> systemVariables, Set<PluginPackageAuthority> pluginPackageAuthorities, Set<PluginPackageRuntimeResourcesDocker> pluginPackageRuntimeResourcesDocker, Set<PluginPackageRuntimeResourcesMysql> pluginPackageRuntimeResourcesMysql, Set<PluginPackageRuntimeResourcesS3> pluginPackageRuntimeResourcesS3, Set<PluginConfig> pluginConfigs, Set<PluginPackageResourceFile> pluginPackageResourceFiles) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.status = status;
        this.uploadTimestamp = uploadTimestamp;
        this.uiPackageIncluded = uiPackageIncluded;
        this.pluginPackageDependencies = pluginPackageDependencies;
        this.pluginPackageMenus = pluginPackageMenus;
        this.pluginPackageEntities = pluginPackageEntities;
        this.systemVariables = systemVariables;
        this.pluginPackageAuthorities = pluginPackageAuthorities;
        this.pluginPackageRuntimeResourcesDocker = pluginPackageRuntimeResourcesDocker;
        this.pluginPackageRuntimeResourcesMysql = pluginPackageRuntimeResourcesMysql;
        this.pluginPackageRuntimeResourcesS3 = pluginPackageRuntimeResourcesS3;
        this.pluginConfigs = pluginConfigs;
        this.pluginPackageResourceFiles = pluginPackageResourceFiles;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(Timestamp uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public boolean isUiPackageIncluded() {
        return uiPackageIncluded;
    }

    public void setUiPackageIncluded(boolean uiPackageIncluded) {
        this.uiPackageIncluded = uiPackageIncluded;
    }

    public Set<PluginPackageDependency> getPluginPackageDependencies() {
        return pluginPackageDependencies;
    }

    public void setPluginPackageDependencies(Set<PluginPackageDependency> pluginPackageDependencies) {
        this.pluginPackageDependencies = pluginPackageDependencies;
    }

    public Set<PluginPackageMenu> getPluginPackageMenus() {
        return pluginPackageMenus;
    }

    public void setPluginPackageMenus(Set<PluginPackageMenu> pluginPackageMenus) {
        this.pluginPackageMenus = pluginPackageMenus;
    }

    public Set<PluginPackageEntity> getPluginPackageEntities() {
        return pluginPackageEntities;
    }

    public void setPluginPackageEntities(Set<PluginPackageEntity> pluginPackageEntities) {
        this.pluginPackageEntities = pluginPackageEntities;
    }

    public Set<SystemVariable> getSystemVariables() {
        return systemVariables;
    }

    public void setSystemVariables(Set<SystemVariable> systemVariables) {
        this.systemVariables = systemVariables;
    }

    public Set<PluginPackageAuthority> getPluginPackageAuthorities() {
        return pluginPackageAuthorities;
    }

    public void setPluginPackageAuthorities(Set<PluginPackageAuthority> pluginPackageAuthorities) {
        this.pluginPackageAuthorities = pluginPackageAuthorities;
    }

    public Set<PluginPackageRuntimeResourcesDocker> getPluginPackageRuntimeResourcesDocker() {
        return pluginPackageRuntimeResourcesDocker;
    }

    public void setPluginPackageRuntimeResourcesDocker(Set<PluginPackageRuntimeResourcesDocker> pluginPackageRuntimeResourcesDocker) {
        this.pluginPackageRuntimeResourcesDocker = pluginPackageRuntimeResourcesDocker;
    }

    public Set<PluginPackageRuntimeResourcesMysql> getPluginPackageRuntimeResourcesMysql() {
        return pluginPackageRuntimeResourcesMysql;
    }

    public void setPluginPackageRuntimeResourcesMysql(Set<PluginPackageRuntimeResourcesMysql> pluginPackageRuntimeResourcesMysql) {
        this.pluginPackageRuntimeResourcesMysql = pluginPackageRuntimeResourcesMysql;
    }

    public Set<PluginPackageRuntimeResourcesS3> getPluginPackageRuntimeResourcesS3() {
        return pluginPackageRuntimeResourcesS3;
    }

    public void setPluginPackageRuntimeResourcesS3(Set<PluginPackageRuntimeResourcesS3> pluginPackageRuntimeResourcesS3) {
        this.pluginPackageRuntimeResourcesS3 = pluginPackageRuntimeResourcesS3;
    }

    public Set<PluginConfig> getPluginConfigs() {
        return pluginConfigs;
    }

    public void setPluginConfigs(Set<PluginConfig> pluginConfigs) {
        this.pluginConfigs = pluginConfigs;
    }

    public Set<PluginPackageResourceFile> getPluginPackageResourceFiles() {
        return pluginPackageResourceFiles;
    }

    public void setPluginPackageResourceFiles(Set<PluginPackageResourceFile> pluginPackageResourceFiles) {
        this.pluginPackageResourceFiles = pluginPackageResourceFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPackage that = (PluginPackage) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, version);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
    }
}
