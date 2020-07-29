package com.webank.wecube.platform.core.lazyDomain.plugin;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.support.DomainIdBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@NamedEntityGraph(name="overview-graph",attributeNodes = @NamedAttributeNode(value = "pluginPackageEntities",subgraph="entities-subgraph"),
    subgraphs = @NamedSubgraph(name = "entities-subgraph",attributeNodes = @NamedAttributeNode("pluginPackageAttributeList")))

@Table(name = "plugin_package_data_model", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"packageName", "version"})
})
public class LazyPluginPackageDataModel {
    @Id
    private String id;

    @Column(name = "version")
    private Integer version = 1;

    @Column
    private String packageName;

    @Column
    private boolean isDynamic;

    @Column
    private String updatePath;

    @Column
    private String updateMethod;

    @Column
    private String updateSource;

    @Column
    private Long updateTime;

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackageDataModel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LazyPluginPackageEntity> pluginPackageEntities = new LinkedHashSet<>();

    public LazyPluginPackageDataModel() {
    }

    public LazyPluginPackageDataModel(String id, Integer version, String packageName, boolean isDynamic, String updatePath, String updateMethod, String updateSource, Long updateTime, Set<LazyPluginPackageEntity> pluginPackageEntities) {
        this.id = id;
        this.version = version;
        this.packageName = packageName;
        this.isDynamic = isDynamic;
        this.updatePath = updatePath;
        this.updateMethod = updateMethod;
        this.updateSource = updateSource;
        this.updateTime = updateTime;
        this.pluginPackageEntities = pluginPackageEntities;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PrePersist
    public void initId() {
        this.id = DomainIdBuilder.buildDomainId(this);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public String getUpdateSource() {
        return updateSource;
    }

    public void setUpdateSource(String updateSource) {
        this.updateSource = updateSource;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Set<LazyPluginPackageEntity> getPluginPackageEntities() {
        return pluginPackageEntities;
    }

    public void setPluginPackageEntities(Set<LazyPluginPackageEntity> pluginPackageEntities) {
        this.pluginPackageEntities = pluginPackageEntities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LazyPluginPackageDataModel)) return false;
        LazyPluginPackageDataModel that = (LazyPluginPackageDataModel) o;
        return getVersion().equals(that.getVersion()) &&
                getPackageName().equals(that.getPackageName()) &&
                getPluginPackageEntities().equals(that.getPluginPackageEntities());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersion(), getPackageName(), getPluginPackageEntities());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
    }
}
