package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicPluginEntityDto {
    private String id;
    private String packageName;
    private String name;
    private String displayName;
    private String description;

    private List<DynamicEntityAttributeDto> attributes = new ArrayList<>();

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

    public List<DynamicEntityAttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DynamicEntityAttributeDto> attributes) {
        this.attributes = attributes;
    }

}
