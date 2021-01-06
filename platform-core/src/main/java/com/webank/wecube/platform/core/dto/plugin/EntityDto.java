package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

public class EntityDto {
    private String packageName;
    private String entityName;
    private List<PluginPackageAttributeDto> attributes = new ArrayList<PluginPackageAttributeDto>();

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

    public EntityDto() {
    }

    public EntityDto(String packageName, String entityName) {
        super();
        this.packageName = packageName;
        this.entityName = entityName;
    }

    public List<PluginPackageAttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PluginPackageAttributeDto> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(PluginPackageAttributeDto attributeDto) {
        if (attributeDto == null) {
            return;
        }

        if (this.attributes == null) {
            this.attributes = new ArrayList<>();
        }

        this.attributes.add(attributeDto);
    }

}
