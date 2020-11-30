//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//import javax.persistence.*;
//import java.util.List;
//
//@Entity
//@Table(name = "plugin_package_entities", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"data_model_id", "name"})
//})
//public class PluginPackageEntity {
//
//    @Id
//    private String id;
//
//    @JsonBackReference
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "data_model_id")
//    private PluginPackageDataModel pluginPackageDataModel;
//
//    @Column(name = "data_model_version")
//    private Integer dataModelVersion = 1;
//
//    @Column(name = "package_name")
//    private String packageName;
//
//    @Column(name = "name")
//    private String name;
//
//    @Column(name = "display_name")
//    private String displayName;
//
//    @Column(name = "description")
//    private String description;
//
//    @JsonManagedReference
//    @OneToMany(mappedBy = "pluginPackageEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    private List<PluginPackageAttribute> pluginPackageAttributeList;
//
//    public PluginPackageEntity() {
//    }
//
//    public PluginPackageEntity(PluginPackageDataModel pluginPackageDataModel, String name,
//                               String displayName,
//                               String description) {
//        this(pluginPackageDataModel, name, displayName, description, null);
//    }
//
//    public PluginPackageEntity(PluginPackageDataModel pluginPackageDataModel, String name,
//                               String displayName,
//                               String description, List<PluginPackageAttribute> pluginPackageAttributes) {
//        setPluginPackageDataModel(pluginPackageDataModel);
//        this.name = name;
//        this.displayName = displayName;
//        this.description = description;
//        this.pluginPackageAttributeList = pluginPackageAttributes;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    @PrePersist
//    public void initId() {
//        this.id = DomainIdBuilder.buildDomainId(this);
//    }
//
//    public PluginPackageDataModel getPluginPackageDataModel() {
//        return pluginPackageDataModel;
//    }
//
//    private void setPluginPackageDataModel(PluginPackageDataModel pluginPackageDataModel) {
//        this.pluginPackageDataModel = pluginPackageDataModel;
//        this.dataModelVersion = pluginPackageDataModel.getVersion();
//        this.packageName = pluginPackageDataModel.getPackageName();
//
//    }
//
//    public Integer getDataModelVersion() {
//        return dataModelVersion;
//    }
//
//    public String getPackageName() {
//        return packageName;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getDisplayName() {
//        return displayName;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public List<PluginPackageAttribute> getPluginPackageAttributeList() {
//        return pluginPackageAttributeList;
//    }
//
//    public void setPluginPackageAttributeList(List<PluginPackageAttribute> pluginPackageAttributeList) {
//        this.pluginPackageAttributeList = pluginPackageAttributeList;
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toString(this);
//    }
//
//}
