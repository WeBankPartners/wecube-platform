package com.webank.wecube.platform.core.support.plugin.dto;

import java.util.ArrayList;
import java.util.List;

public class RegisteredEntityDefDto {
    private String id;
    private String packageName;
    private String name;
    private String displayName;
    private String description;

    private List<RegisteredEntityAttrDefDto> attributes = new ArrayList<>();

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

    public List<RegisteredEntityAttrDefDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<RegisteredEntityAttrDefDto> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RegisteredEntityDefDto [id=");
        builder.append(id);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append(", name=");
        builder.append(name);
        builder.append(", displayName=");
        builder.append(displayName);
        builder.append(", description=");
        builder.append(description);
        builder.append(", attributes=");
        builder.append(attributes);
        builder.append("]");
        return builder.toString();
    }

    
}
