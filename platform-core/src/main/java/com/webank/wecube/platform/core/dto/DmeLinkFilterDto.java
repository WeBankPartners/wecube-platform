package com.webank.wecube.platform.core.dto;

import java.util.ArrayList;
import java.util.List;

public class DmeLinkFilterDto {
    int index;
    String packageName;
    String entityName;
    List<FilterDto> attributeFilters;

    public DmeLinkFilterDto() {
        this.attributeFilters = new ArrayList<>();
    }

    public DmeLinkFilterDto(int index, String packageName, String entityName, List<FilterDto> attributeFilters) {
        this.index = index;
        this.packageName = packageName;
        this.entityName = entityName;
        this.attributeFilters = attributeFilters;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    public List<FilterDto> getAttributeFilters() {
        return attributeFilters;
    }

    public void setAttributeFilters(List<FilterDto> attributeFilters) {
        this.attributeFilters = attributeFilters;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DmeLinkFilterDto [index=");
        builder.append(index);
        builder.append(", packageName=");
        builder.append(packageName);
        builder.append(", entityName=");
        builder.append(entityName);
        builder.append(", attributeFilters=");
        builder.append(attributeFilters);
        builder.append("]");
        return builder.toString();
    }
    
    
}
