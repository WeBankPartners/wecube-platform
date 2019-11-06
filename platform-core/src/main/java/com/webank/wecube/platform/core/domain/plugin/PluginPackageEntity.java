package com.webank.wecube.platform.core.domain.plugin;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "plugin_package_entities", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plugin_package_data_model_id", "name"})
})
public class PluginPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonBackReference
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "plugin_package_data_model_id")
    private PluginPackageDataModel pluginPackageDataModel;

    @Column(name = "name")
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description")
    private String description;

    @JsonManagedReference
    @OneToMany(mappedBy = "pluginPackageEntity", cascade = CascadeType.ALL)
    private List<PluginPackageAttribute> pluginPackageAttributeList;

    public PluginPackageEntity() {
    }

    public PluginPackageEntity(PluginPackageDataModel pluginPackageDataModel, String name,
                               String displayName,
                               String description) {
        this.pluginPackageDataModel = pluginPackageDataModel;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PluginPackageDataModel getPluginPackageDataModel() {
        return pluginPackageDataModel;
    }

    public void setPluginPackageDataModel(PluginPackageDataModel pluginPackageDataModel) {
        this.pluginPackageDataModel = pluginPackageDataModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PluginPackageAttribute> getPluginPackageAttributeList() {
        return pluginPackageAttributeList;
    }

    public void setPluginPackageAttributeList(List<PluginPackageAttribute> pluginPackageAttributeList) {
        this.pluginPackageAttributeList = pluginPackageAttributeList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackage"});
    }

}
