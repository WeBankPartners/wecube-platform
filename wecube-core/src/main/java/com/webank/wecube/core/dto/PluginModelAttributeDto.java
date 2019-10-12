package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;

public class PluginModelAttributeDto {
    @NotNull
    private String packageName;
    private String packageVersion;
    private String entityName;
    private String name;
    private String description;
    private String dataType;
    private String state = "draft";
    private String refPackageName;
    private String refEntityName;
    private String refAttributeName;
    private String refPackageVersion;

    public PluginModelAttributeDto(String name,
                                   String description,
                                   String dataType,
                                   String referencePackageName,
                                   String referencePackageVersion,
                                   String referenceEntityName,
                                   String referenceAttributeName) {
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.refPackageName = referencePackageName;
        this.refPackageVersion = referencePackageVersion;
        this.refEntityName = referenceEntityName;
        this.refAttributeName = referenceAttributeName;
    }

    public PluginModelAttributeDto() {
    }


    /**
     * @param pluginModelAttribute input attribute domain object
     * @return attribute dto exposed to the server
     */
    public static PluginModelAttributeDto fromDomain(PluginModelAttribute pluginModelAttribute) {
        PluginModelAttributeDto pluginModelAttributeDto = new PluginModelAttributeDto();
        pluginModelAttributeDto.setPackageName(pluginModelAttribute.getPluginModelEntity().getPluginPackage().getName());
        pluginModelAttributeDto.setPackageVersion(pluginModelAttribute.getPluginModelEntity().getPluginPackage().getVersion());
        pluginModelAttributeDto.setEntityName(pluginModelAttribute.getPluginModelEntity().getName());
        pluginModelAttributeDto.setName(pluginModelAttribute.getName());
        pluginModelAttributeDto.setDescription(pluginModelAttribute.getDescription());
        pluginModelAttributeDto.setDataType(pluginModelAttribute.getDataType());
        pluginModelAttributeDto.setState(pluginModelAttribute.getState());
        if (pluginModelAttribute.getPluginModelAttribute() != null) {
            pluginModelAttributeDto.setRefPackageName(pluginModelAttribute.getPluginModelAttribute().getPluginModelEntity().getPluginPackage().getName());
            pluginModelAttributeDto.setRefPackageVersion(pluginModelAttribute.getPluginModelAttribute().getPluginModelEntity().getPluginPackage().getVersion());
            pluginModelAttributeDto.setRefEntityName(pluginModelAttribute.getPluginModelAttribute().getPluginModelEntity().getName());
            pluginModelAttributeDto.setRefAttributeName(pluginModelAttribute.getPluginModelAttribute().getName());
        }


        return pluginModelAttributeDto;
    }

    /**
     * @param attributeDto       input attribute dto
     * @param referenceAttribute the attribute this attribute refers to
     * @param referenceEntity    the entity this attribute refers to
     * @return transformed attribute domain object
     */
    public static PluginModelAttribute toDomain(PluginModelAttributeDto attributeDto,
                                                PluginModelAttribute referenceAttribute,
                                                PluginModelEntity referenceEntity) {
        PluginModelAttribute pluginModelAttribute = new PluginModelAttribute();


        if (referenceEntity != null) {
            pluginModelAttribute.setPluginModelEntity(referenceEntity);
        }

        if (referenceAttribute != null) {
            pluginModelAttribute.setPluginModelAttribute(referenceAttribute);
        }

        if (attributeDto.getName() != null) {
            pluginModelAttribute.setName(attributeDto.getName());
        }

        if (attributeDto.getDescription() != null) {
            // the description can be empty
            pluginModelAttribute.setDescription(attributeDto.getDescription());
        }

        if (!StringUtils.isEmpty(attributeDto.getDataType())) {
            // the DataType should not be null or empty
            pluginModelAttribute.setDataType(attributeDto.getDataType().toLowerCase());
        }


        if (!StringUtils.isEmpty(attributeDto.getState())) {
            // the State should not be null or empty
            pluginModelAttribute.setState(attributeDto.getState().toLowerCase());
        }

        return pluginModelAttribute;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
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

    public String getRefPackageVersion() {
        return refPackageVersion;
    }

    public void setRefPackageVersion(String refPackageVersion) {
        this.refPackageVersion = refPackageVersion;
    }
}
