//package com.webank.wecube.platform.core.domain.plugin;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.webank.wecube.platform.core.support.DomainIdBuilder;
//import com.webank.wecube.platform.core.utils.constant.DataModelDataType;
//import org.apache.commons.lang.builder.ReflectionToStringBuilder;
//
//import javax.persistence.*;
//
//@Entity
//@Table(name = "plugin_package_attributes", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"entity_id", "name"})
//})
//public class PluginPackageAttribute {
//    @Id
//    private String id;
//    
//    @JsonBackReference
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "entity_id")
//    private PluginPackageEntity pluginPackageEntity;
//
//    @JsonManagedReference
//    @JsonBackReference
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "reference_id", referencedColumnName="id")
//    private PluginPackageAttribute pluginPackageAttribute;
//
//    @Column(name = "name")
//    private String name;
//
//    @Column(name = "description")
//    private String description;
//
//    @Column(name = "data_type")
//    private String dataType;
//
//
//    public PluginPackageAttribute() {
//    }
//
//    public PluginPackageAttribute(PluginPackageEntity pluginPackageEntity,
//                                  PluginPackageAttribute pluginPackageAttribute,
//                                  String name,
//                                  String description,
//                                  String dataType) {
//        this.pluginPackageEntity = pluginPackageEntity;
//        this.pluginPackageAttribute = pluginPackageAttribute;
//        this.name = name;
//        this.description = description;
//        this.dataType = DataModelDataType.fromCode(dataType).getCode();
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
//    public void setId(String id) {
//        this.id = id;
//    }
//    public PluginPackageEntity getPluginPackageEntity() {
//        return pluginPackageEntity;
//    }
//
//    public void setPluginPackageEntity(PluginPackageEntity pluginPackageEntity) {
//        this.pluginPackageEntity = pluginPackageEntity;
//    }
//
//    public PluginPackageAttribute getPluginPackageAttribute() {
//        return pluginPackageAttribute;
//    }
//
//    public void setPluginPackageAttribute(PluginPackageAttribute pluginPackageAttribute) {
//        this.pluginPackageAttribute = pluginPackageAttribute;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getDataType() {
//        return dataType.toLowerCase();
//    }
//
//    public void setDataType(String dataType) {
//        this.dataType = DataModelDataType.fromCode(dataType).getCode();
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toStringExclude(this, new String[]{"pluginPackageEntity", "pluginPackageAttribute"});
//    }
//}
