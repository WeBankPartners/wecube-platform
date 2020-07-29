package com.webank.wecube.platform.core.lazyDomain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.support.DomainIdBuilder;
import com.webank.wecube.platform.core.utils.constant.DataModelDataType;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "plugin_package_attributes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"entity_id", "name"})
})
public class LazyPluginPackageAttribute {
    @Id
    private String id;

    @JsonBackReference
    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
    private LazyPluginPackageEntity pluginPackageEntity;

    @JsonManagedReference
    @JsonBackReference
    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_id", referencedColumnName="id")
    private LazyPluginPackageAttribute pluginPackageAttribute;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "data_type")
    private String dataType;


    public LazyPluginPackageAttribute() {
    }

    public LazyPluginPackageAttribute(LazyPluginPackageEntity pluginPackageEntity,
                                      LazyPluginPackageAttribute pluginPackageAttribute,
                                      String name,
                                      String description,
                                      String dataType) {
        this.pluginPackageEntity = pluginPackageEntity;
        this.pluginPackageAttribute = pluginPackageAttribute;
        this.name = name;
        this.description = description;
        this.dataType = DataModelDataType.fromCode(dataType).getCode();
    }

    public String getId() {
        return id;
    }

    @PrePersist
    public void initId() {
        this.id = DomainIdBuilder.buildDomainId(this);
    }

    public void setId(String id) {
        this.id = id;
    }
    public LazyPluginPackageEntity getPluginPackageEntity() {
        return pluginPackageEntity;
    }

    public void setPluginPackageEntity(LazyPluginPackageEntity pluginPackageEntity) {
        this.pluginPackageEntity = pluginPackageEntity;
    }

    public LazyPluginPackageAttribute getPluginPackageAttribute() {
        return pluginPackageAttribute;
    }

    public void setPluginPackageAttribute(LazyPluginPackageAttribute pluginPackageAttribute) {
        this.pluginPackageAttribute = pluginPackageAttribute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataType() {
        return dataType.toLowerCase();
    }

    public void setDataType(String dataType) {
        this.dataType = DataModelDataType.fromCode(dataType).getCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackageEntity", "pluginPackageAttribute"});
    }
}
