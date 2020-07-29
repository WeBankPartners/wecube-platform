package com.webank.wecube.platform.core.lazyDomain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.support.DomainIdBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "plugin_package_entities", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"data_model_id", "name"})
})
public class LazyPluginPackageEntity {

    @Id
    private String id;

    @JsonBackReference
    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "data_model_id")
    private LazyPluginPackageDataModel pluginPackageDataModel;

    @Column(name = "data_model_version")
    private Integer dataModelVersion = 1;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "name")
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description")
    private String description;

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackageEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LazyPluginPackageAttribute> pluginPackageAttributeList;

    public LazyPluginPackageEntity() {
    }

    public LazyPluginPackageEntity(LazyPluginPackageDataModel pluginPackageDataModel, String name,
                                   String displayName,
                                   String description) {
        this(pluginPackageDataModel, name, displayName, description, null);
    }

    public LazyPluginPackageEntity(LazyPluginPackageDataModel pluginPackageDataModel, String name,
                                   String displayName,
                                   String description, List<LazyPluginPackageAttribute> pluginPackageAttributes) {
        setPluginPackageDataModel(pluginPackageDataModel);
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.pluginPackageAttributeList = pluginPackageAttributes;
    }

    public String getId() {
        return id;
    }

    @PrePersist
    public void initId() {
        this.id = DomainIdBuilder.buildDomainId(this);
    }

    public LazyPluginPackageDataModel getPluginPackageDataModel() {
        return pluginPackageDataModel;
    }

    private void setPluginPackageDataModel(LazyPluginPackageDataModel pluginPackageDataModel) {
        this.pluginPackageDataModel = pluginPackageDataModel;
        this.dataModelVersion = pluginPackageDataModel.getVersion();
        this.packageName = pluginPackageDataModel.getPackageName();

    }

    public Integer getDataModelVersion() {
        return dataModelVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<LazyPluginPackageAttribute> getPluginPackageAttributeList() {
        return pluginPackageAttributeList;
    }

    public void setPluginPackageAttributeList(List<LazyPluginPackageAttribute> pluginPackageAttributeList) {
        this.pluginPackageAttributeList = pluginPackageAttributeList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
