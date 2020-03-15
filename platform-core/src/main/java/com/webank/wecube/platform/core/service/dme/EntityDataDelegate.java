package com.webank.wecube.platform.core.service.dme;

import java.util.Map;

public class EntityDataDelegate {
    public static final String UNIQUE_IDENTIFIER = "id";
    public static final String VISUAL_FIELD = "displayName";
    private String id;
    private String displayName;
    private String queryAttrName;
    private Object queryAttrValue;
    private Map<String, Object> entityData;
    

    public Object getAttributeValue(String attrName) {
        if (entityData == null) {
            return null;
        }
        return this.entityData.get(attrName);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, Object> getEntityData() {
        return entityData;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEntityData(Map<String, Object> entityData) {
        this.entityData = entityData;
    }

    public String getQueryAttrName() {
        return queryAttrName;
    }

    public void setQueryAttrName(String queryAttrName) {
        this.queryAttrName = queryAttrName;
    }

    public Object getQueryAttrValue() {
        return queryAttrValue;
    }

    public void setQueryAttrValue(Object queryAttrValue) {
        this.queryAttrValue = queryAttrValue;
    }

    public boolean hasQueryAttribute(){
        return (this.queryAttrName != null);
    }
}
