package com.webank.wecube.platform.core.entity.plugin;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.utils.JsonUtils;

public class ResourceItem {
    private String id;

    private String additionalProperties;

    private String createdBy;

    private Date createdDate;

    private Integer isAllocated;

    private String name;

    private String purpose;

    private String resourceServerId;

    private String status;

    private String type;

    private String updatedBy;

    private Date updatedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties == null ? null : additionalProperties.trim();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getIsAllocated() {
        return isAllocated;
    }

    public void setIsAllocated(Integer isAllocated) {
        this.isAllocated = isAllocated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose == null ? null : purpose.trim();
    }

    public String getResourceServerId() {
        return resourceServerId;
    }

    public void setResourceServerId(String resourceServerId) {
        this.resourceServerId = resourceServerId == null ? null : resourceServerId.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy == null ? null : updatedBy.trim();
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public Map<String, String> getAdditionalPropertiesMap() {
        if (additionalProperties != null) {
            return convertToMap(additionalProperties);
        }
        return new HashMap<String, String>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertToMap(String additionalProperties) {
        try {
            return JsonUtils.toObject(additionalProperties, Map.class);
        } catch (IOException e) {
            throw new WecubeCoreException(String.format("Failed to parse resource_item.additional_properties [%s] : Invalid json format.", additionalProperties), e);
        }
    }
}