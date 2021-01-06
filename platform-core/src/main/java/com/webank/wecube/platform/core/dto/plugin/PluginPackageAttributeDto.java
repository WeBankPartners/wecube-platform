package com.webank.wecube.platform.core.dto.plugin;

public class PluginPackageAttributeDto {
    private String id;
    private String packageName;
    private String entityName;
    private String name;
    private String description;
    private String dataType;
    private String refPackageName;
    private String refEntityName;
    private String refAttributeName;

//    public PluginPackageAttributeDto(String attributeId,
//                                     String name,
//                                     String description,
//                                     String dataType,
//                                     String referencePackageName,
//                                     String referenceEntityName,
//                                     String referenceAttributeName) {
//        this.id = attributeId;
//        this.name = name;
//        this.description = description;
//        this.dataType = dataType;
//        this.refPackageName = referencePackageName;
//        this.refEntityName = referenceEntityName;
//        this.refAttributeName = referenceAttributeName;
//    }

//    public PluginPackageAttributeDto() {
//    }
//

   

    

//    /**
//     * @param attributeDto       input attribute dto
//     * @param referenceAttribute the attribute this attribute refers to
//     * @param referenceEntity    the entity this attribute refers to
//     * @return transformed attribute domain object
//     */
//    public static PluginPackageAttributes toDomain(PluginPackageAttributeDto attributeDto,
//                                                  PluginPackageAttributes referenceAttribute,
//                                                  PluginPackageEntities referenceEntity) {
//        PluginPackageAttributes pluginPackageAttribute = new PluginPackageAttributes();
//
//
//        if (referenceEntity != null) {
//            pluginPackageAttribute.setPluginPackageEntities(referenceEntity);
//        }
//
//        if (referenceAttribute != null) {
//            pluginPackageAttribute.setPluginPackageAttribute(referenceAttribute);
//        }
//
//        if (attributeDto.getName() != null) {
//            pluginPackageAttribute.setName(attributeDto.getName());
//        }
//
//        if (attributeDto.getDescription() != null) {
//            // the description can be empty
//            pluginPackageAttribute.setDescription(attributeDto.getDescription());
//        }
//
//        if (!StringUtils.isEmpty(attributeDto.getDataType())) {
//            // the DataType should not be null or empty
//            pluginPackageAttribute.setDataType(attributeDto.getDataType().toLowerCase());
//        }
//
//        return pluginPackageAttribute;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getRefPackageName() {
        return refPackageName;
    }

    public void setRefPackageName(String refPackageName) {
        this.refPackageName = refPackageName;
    }

    public String getRefEntityName() {
        return refEntityName;
    }

    public void setRefEntityName(String refEntityName) {
        this.refEntityName = refEntityName;
    }

    public String getRefAttributeName() {
        return refAttributeName;
    }

    public void setRefAttributeName(String refAttributeName) {
        this.refAttributeName = refAttributeName;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        PluginPackageAttributeDto that = (PluginPackageAttributeDto) o;
//        return getPackageName().equals(that.getPackageName()) &&
//                getEntityName().equals(that.getEntityName()) &&
//                getName().equals(that.getName()) &&
//                Objects.equals(getDescription(), that.getDescription()) &&
//                getDataType().equals(that.getDataType()) &&
//                Objects.equals(getRefPackageName(), that.getRefPackageName()) &&
//                Objects.equals(getRefEntityName(), that.getRefEntityName()) &&
//                Objects.equals(getRefAttributeName(), that.getRefAttributeName());
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(getPackageName(), getEntityName(), getName(), getDescription(), getDataType(), getRefPackageName(), getRefEntityName(), getRefAttributeName());
//    }
//
//    @Override
//    public String toString() {
//        return ReflectionToStringBuilder.toString(this);
//    }
}
