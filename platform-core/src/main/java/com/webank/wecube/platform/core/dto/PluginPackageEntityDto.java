package com.webank.wecube.platform.core.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginPackageEntityDto {
    private String id;
    private String packageName;
    private String name;
    private String displayName;
    private String description;

    private Integer dataModelVersion;
    private List<TrimmedPluginPackageEntityDto> referenceToEntityList = new ArrayList<>();
    private List<TrimmedPluginPackageEntityDto> referenceByEntityList = new ArrayList<>();
    private List<PluginPackageAttributeDto> attributes = new ArrayList<>();


    public PluginPackageEntityDto(String id, String name, String displayName, String description, List<PluginPackageAttributeDto> attributes) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.attributes = attributes;
    }

    public PluginPackageEntityDto() {
    }

    /**
     * @param pluginPackageEntityDto input entity dto
     * @param dataModel
     * @return transformed entity domain object
     */
    public static PluginPackageEntities toDomain(PluginPackageEntityDto pluginPackageEntityDto, PluginPackageDataModel dataModel) {

        PluginPackageEntities pluginPackageEntity = new PluginPackageEntities();
        pluginPackageEntity.setPluginPackageDataModel(dataModel);
        pluginPackageEntity.setName(pluginPackageEntityDto.getName());
        pluginPackageEntity.setDisplayName(pluginPackageEntityDto.getDisplayName());
        pluginPackageEntity.setDescription(pluginPackageEntityDto.getDescription());
        

        if (pluginPackageEntityDto.getAttributes() != null) {
            List<PluginPackageAttributes> pluginPackageAttributeList = new ArrayList<>();
            for (PluginPackageAttributeDto pluginPackageAttributeDto : pluginPackageEntityDto.getAttributes()) {
                pluginPackageAttributeList.add(PluginPackageAttributeDto.toDomain(pluginPackageAttributeDto, null, pluginPackageEntity));
            }
            pluginPackageEntity.setPluginPackageAttributes(pluginPackageAttributeList);
        }

        return pluginPackageEntity;
    }

    public TrimmedPluginPackageEntityDto toTrimmedPluginPackageEntityDto() {
        return new TrimmedPluginPackageEntityDto(getId(), getPackageName(), getDataModelVersion(), getName(), getDisplayName());
    }

    public void updateReferenceBy(String entityId, String packageName, Integer dataModelVersion, String name,
            String displayName, PluginPackageAttributeDto relatedAttribute) {
        TrimmedPluginPackageEntityDto trimmedPluginPackageEntityDto = new TrimmedPluginPackageEntityDto(entityId,
                packageName, dataModelVersion, name, displayName, relatedAttribute);
        this.referenceByEntityList.add(trimmedPluginPackageEntityDto);
    }

    public void updateReferenceBy(TrimmedPluginPackageEntityDto trimmedEntityDto) {
        updateReferenceBy(trimmedEntityDto.getId(), trimmedEntityDto.getPackageName(),
                trimmedEntityDto.getDataModelVersion(), trimmedEntityDto.getName(), trimmedEntityDto.getDisplayName(),
                trimmedEntityDto.getRelatedAttribute());
    }

    public void updateReferenceTo(String entityId, String packageName, Integer dataModelVersion, String name,
            String displayName, PluginPackageAttributeDto relatedAttribute) {
        TrimmedPluginPackageEntityDto trimmedPluginPackageEntityDto = new TrimmedPluginPackageEntityDto(entityId,
                packageName, dataModelVersion, name, displayName, relatedAttribute);
        this.referenceToEntityList.add(trimmedPluginPackageEntityDto);
    }

    public void updateReferenceTo(TrimmedPluginPackageEntityDto trimmedEntityDto) {
        updateReferenceTo(trimmedEntityDto.getId(), trimmedEntityDto.getPackageName(),
                trimmedEntityDto.getDataModelVersion(), trimmedEntityDto.getName(), trimmedEntityDto.getDisplayName(),
                trimmedEntityDto.getRelatedAttribute());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public Integer getDataModelVersion() {
        return dataModelVersion;
    }

    public void setDataModelVersion(Integer dataModelVersion) {
        this.dataModelVersion = dataModelVersion;
    }

    public List<PluginPackageAttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PluginPackageAttributeDto> attributes) {
        this.attributes = attributes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<TrimmedPluginPackageEntityDto> getReferenceToEntityList() {
        return referenceToEntityList;
    }

    public void setReferenceToEntityList(List<TrimmedPluginPackageEntityDto> referenceToEntityList) {
        this.referenceToEntityList = referenceToEntityList;
    }

    public List<TrimmedPluginPackageEntityDto> getReferenceByEntityList() {
        return referenceByEntityList;
    }

    public void setReferenceByEntityList(List<TrimmedPluginPackageEntityDto> referenceByEntityList) {
        this.referenceByEntityList = referenceByEntityList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public static class TrimmedPluginPackageEntityDto {
        private String id;
        private String packageName;
        private Integer dataModelVersion;
        private String name;
        private String displayName;
        private PluginPackageAttributeDto relatedAttribute;

        public TrimmedPluginPackageEntityDto() {
        }

        public TrimmedPluginPackageEntityDto(String entityId, String packageName, Integer dataModelVersion, String name,
                String displayName, PluginPackageAttributeDto relatedAttribute) {
            this.id = entityId;
            this.packageName = packageName;
            this.name = name;
            this.displayName = displayName;
            this.dataModelVersion = dataModelVersion;
            this.relatedAttribute = relatedAttribute;
        }

        public TrimmedPluginPackageEntityDto(String entityId, String packageName, Integer dataModelVersion, String name,
                String displayName) {
            this.id = entityId;
            this.packageName = packageName;
            this.name = name;
            this.displayName = displayName;
            this.dataModelVersion = dataModelVersion;
        }
        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public Integer getDataModelVersion() {
            return dataModelVersion;
        }

        @JsonIgnore
        public PluginPackageEntityKey getPluginPackageEntityKey() {
            return new PluginPackageEntityKey(getPackageName(), getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TrimmedPluginPackageEntityDto that = (TrimmedPluginPackageEntityDto) o;

            return new EqualsBuilder()
                    .append(getPackageName(), that.getPackageName())
                    .append(getName(), that.getName())
                    .append(getDisplayName(), that.getDisplayName())
                    .append(getDataModelVersion(), that.getDataModelVersion())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(getPackageName())
                    .append(getName())
                    .append(getDisplayName())
                    .append(getDataModelVersion())
                    .toHashCode();
        }

        public PluginPackageAttributeDto getRelatedAttribute() {
            return relatedAttribute;
        }

        public void setRelatedAttribute(PluginPackageAttributeDto relatedAttribute) {
            this.relatedAttribute = relatedAttribute;
        }
    }

    public static class PluginPackageEntityKey {
        String packageName;
        String entityName;

        public PluginPackageEntityKey() {
        }

        public PluginPackageEntityKey(String packageName, String entityName) {
            this.packageName = packageName;
            this.entityName = entityName;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PluginPackageEntityKey that = (PluginPackageEntityKey) o;
            return getPackageName().equals(that.getPackageName()) &&
                    getEntityName().equals(that.getEntityName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPackageName(), getEntityName());
        }


    }
}
