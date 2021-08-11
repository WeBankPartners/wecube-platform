package com.webank.wecube.platform.core.entity.plugin;

public class PluginPackageAttributes {
    private String id;

    private String entityId;

    private String referenceId;

    private String name;

    private String description;

    private String dataType;
    
    private Boolean mandatory = false;
    
    private String refPackage;
    
    private String refEntity;
    
    private String refAttr;
    
    private String multiple;

    private transient PluginPackageEntities pluginPackageEntities;

    private transient PluginPackageAttributes pluginPackageAttribute;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId == null ? null : entityId.trim();
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId == null ? null : referenceId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType == null ? null : dataType.trim();
    }

    public PluginPackageEntities getPluginPackageEntities() {
        return pluginPackageEntities;
    }

    public void setPluginPackageEntities(PluginPackageEntities pluginPackageEntities) {
        this.pluginPackageEntities = pluginPackageEntities;
    }

    public PluginPackageAttributes getPluginPackageAttribute() {
        return pluginPackageAttribute;
    }

    public void setPluginPackageAttribute(PluginPackageAttributes pluginPackageAttribute) {
        this.pluginPackageAttribute = pluginPackageAttribute;
    }

    public String getRefPackage() {
        return refPackage;
    }

    public void setRefPackage(String refPackage) {
        this.refPackage = refPackage;
    }

    public String getRefEntity() {
        return refEntity;
    }

    public void setRefEntity(String refEntity) {
        this.refEntity = refEntity;
    }

    public String getRefAttr() {
        return refAttr;
    }

    public void setRefAttr(String refAttr) {
        this.refAttr = refAttr;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

}