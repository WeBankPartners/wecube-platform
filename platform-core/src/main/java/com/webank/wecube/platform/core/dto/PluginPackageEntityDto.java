package com.webank.wecube.platform.core.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    public PluginPackageEntityDto(String id, String name, String displayName, String description,
            List<PluginPackageAttributeDto> attributes) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.attributes = attributes;
    }

    public PluginPackageEntityDto() {
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

        public PluginPackageAttributeDto getRelatedAttribute() {
            return relatedAttribute;
        }

        public void setRelatedAttribute(PluginPackageAttributeDto relatedAttribute) {
            this.relatedAttribute = relatedAttribute;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public void setDataModelVersion(Integer dataModelVersion) {
            this.dataModelVersion = dataModelVersion;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        
    }

}
