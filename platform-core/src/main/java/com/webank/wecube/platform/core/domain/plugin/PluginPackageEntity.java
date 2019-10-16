package com.webank.wecube.platform.core.domain.plugin;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "plugin_package_entities", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plugin_package_id", "name"})
})
public class PluginPackageEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "plugin_package_id")
    private PluginPackage pluginPackage;

    @Column(name = "name")
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description")
    private String description;


    @OneToMany(mappedBy = "pluginPackageEntity", cascade = CascadeType.ALL)
    private List<PluginPackageAttribute> pluginPackageAttributeList;

    public PluginPackageEntity() {
    }

    public PluginPackageEntity(PluginPackage pluginPackage,
                               String name,
                               String displayName,
                               String description) {
        this.pluginPackage = pluginPackage;
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

    public PluginPackage getPluginPackage() {
        return pluginPackage;
    }

    public void setPluginPackage(PluginPackage pluginPackage) {
        this.pluginPackage = pluginPackage;
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
}
