package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "plugin_package_data_model", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plugin_package_id", "version"})
})
public class PluginPackageDataModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "version")
    private long version = 1;

    @JsonBackReference
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "plugin_package_id")
    private PluginPackage pluginPackage;

    @Column
    private String packageName;

    @Column
    private String packageVersion;

    @Column
    private boolean isDynamic;

    @Column
    private String updatePath;

    @Column
    private String updateMethod;

    @Column
    private Timestamp updateTimestamp;

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackageDataModel", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PluginPackageEntity> pluginPackageEntities = new LinkedHashSet<>();

    public PluginPackageDataModel() {
    }

    public PluginPackageDataModel(Integer id, Long version, PluginPackage pluginPackage, String packageName, String packageVersion, boolean isDynamic, String updatePath, String updateMethod, Timestamp updateTimestamp, Set<PluginPackageEntity> pluginPackageEntities) {
        this.id = id;
        this.version = version;
        this.pluginPackage = pluginPackage;
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        this.isDynamic = isDynamic;
        this.updatePath = updatePath;
        this.updateMethod = updateMethod;
        this.updateTimestamp = updateTimestamp;
        this.pluginPackageEntities = pluginPackageEntities;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public PluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(PluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
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

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }

    public String getUpdatePath() {
        return updatePath;
    }

    public void setUpdatePath(String updatePath) {
        this.updatePath = updatePath;
    }

    public String getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(String updateMethod) {
        this.updateMethod = updateMethod;
    }

    public Timestamp getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Timestamp updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Set<PluginPackageEntity> getPluginPackageEntities() {
        return pluginPackageEntities;
    }

    public void setPluginPackageEntities(Set<PluginPackageEntity> pluginPackageEntities) {
        this.pluginPackageEntities = pluginPackageEntities;
    }
}
