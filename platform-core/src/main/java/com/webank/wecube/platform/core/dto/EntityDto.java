package com.webank.wecube.platform.core.dto;

import java.util.ArrayList;
import java.util.List;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;

public class EntityDto {
    private String packageName;
    private String entityName;
    private List<PluginPackageAttribute> attributes = new ArrayList<PluginPackageAttribute>();

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

    public List<PluginPackageAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PluginPackageAttribute> attributes) {
        this.attributes = attributes;
    }

}
