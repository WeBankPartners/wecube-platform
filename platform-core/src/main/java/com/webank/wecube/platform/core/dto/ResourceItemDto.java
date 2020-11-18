package com.webank.wecube.platform.core.dto;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.service.resource.ResourceItemStatus;
import com.webank.wecube.platform.core.service.resource.ResourceItemType;
import com.webank.wecube.platform.core.utils.JsonUtils;

@JsonInclude(Include.NON_NULL)
public class ResourceItemDto {
    private String id;
    private String name;
    private String type;
    private String additionalProperties;
    private String resourceServerId;
    private Boolean isAllocated;
    private String purpose;
    private String status;
    private String createdBy;
    private long createdDate;
    private String updatedBy;
    private long updatedDate;
    private ResourceServerDto resourceServer;

    public static ResourceItemDto fromDomain(ResourceItem resourceItem) {
        ResourceItemDto resourceItemDto = new ResourceItemDto();
        resourceItemDto.setId(resourceItem.getId());
        resourceItemDto.setName(resourceItem.getName());
        resourceItemDto.setType(resourceItem.getType());
        resourceItemDto.setAdditionalProperties(resourceItem.getAdditionalProperties());
        resourceItemDto.setResourceServerId(resourceItem.getResourceServerId());
        if (resourceItem.getResourceServer() != null) {
            resourceItemDto.setResourceServer(ResourceServerDto.fromDomain(resourceItem.getResourceServer()));
        }
        resourceItemDto.setIsAllocated(
                resourceItem.getIsAllocated() != null && resourceItem.getIsAllocated() == 1 ? true : false);
        resourceItemDto.setPurpose(resourceItem.getPurpose());
        resourceItemDto.setStatus(resourceItem.getStatus());
        resourceItemDto.setCreatedBy(resourceItem.getCreatedBy());
        resourceItemDto.setCreatedDate(resourceItem.getCreatedDate().getTime());
        resourceItemDto.setUpdatedBy(resourceItem.getUpdatedBy());
        resourceItemDto.setUpdatedDate(resourceItem.getUpdatedDate().getTime());
        return resourceItemDto;
    }

    public static ResourceItem toDomain(ResourceItemDto resourceItemDto, ResourceItem existedResourceItem) {
        ResourceItem resourceItem = existedResourceItem;
        if (resourceItem == null) {
            resourceItem = new ResourceItem();
        }

        if (resourceItemDto.getId() != null) {
            resourceItem.setId(resourceItemDto.getId());
        }

        if (resourceItemDto.getName() != null) {
            resourceItem.setName(resourceItemDto.getName());
        }

        if (resourceItemDto.getType() != null) {
            validateItemType(resourceItemDto.getType());
            resourceItem.setType(resourceItemDto.getType());
        }

        if (resourceItemDto.getAdditionalProperties() != null) {
            resourceItem.setAdditionalProperties(resourceItemDto.getAdditionalProperties());
        }

        if (resourceItemDto.getResourceServerId() != null) {
            resourceItem.setResourceServerId(resourceItemDto.getResourceServerId());
        }

        if (resourceItemDto.getIsAllocated() != null) {
            resourceItem.setIsAllocated(
                    resourceItemDto.getIsAllocated() != null && resourceItemDto.getIsAllocated() ? 1 : 0);
        }

        if (resourceItemDto.getPurpose() != null) {
            resourceItem.setPurpose(resourceItemDto.getPurpose());
        }

        if (resourceItemDto.getStatus() != null) {
            validateItemStatus(resourceItemDto.getStatus());
            resourceItem.setStatus(resourceItemDto.getStatus());
        }

        updateSystemFieldsWithDefaultValues(resourceItem);

        return resourceItem;
    }

    private static void updateSystemFieldsWithDefaultValues(ResourceItem resourceItem) {
        if (resourceItem.getStatus() == null) {
            resourceItem.setStatus(ResourceItemStatus.CREATED.getCode());
        }

        if (resourceItem.getCreatedBy() == null) {
            resourceItem.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        }

        if (resourceItem.getCreatedDate() == null) {
            resourceItem.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        }

        resourceItem.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        resourceItem.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
    }

    private static void validateItemType(String itemType) {
        if (ResourceItemType.fromCode(itemType) == ResourceItemType.NONE) {
            String errMsg = String.format("Unsupported resource item type [%s].", itemType);
            throw new WecubeCoreException("3286", errMsg, itemType);
        }
    }

    private static void validateItemStatus(String status) {
        if (ResourceItemStatus.fromCode(status) == ResourceItemStatus.NONE) {
            String msg = String.format("Unsupported resource item status [%s].", status);
            throw new WecubeCoreException("3287", msg, status);
        }
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
            String msg = String.format(
                    "Failed to parse resource_item.additional_properties [%s] : Invalid json format.",
                    additionalProperties);
            throw new WecubeCoreException("3288", msg, additionalProperties);
        }
    }

    public ResourceItemDto(String name, String type, String additionalProperties, String resourceServerId,
            String purpose) {
        super();
        this.name = name;
        this.type = type;
        this.additionalProperties = additionalProperties;
        this.resourceServerId = resourceServerId;
        this.purpose = purpose;
    }

    public ResourceItemDto(String id, String name, String type, String additionalProperties, String resourceServerId,
            Boolean isAllocated, String purpose, String status, String createdBy, long createdDate, String updatedBy,
            long updatedDate, ResourceServerDto resourceServer) {
        super();
        this.id = id;
        this.name = name;
        this.type = type;
        this.additionalProperties = additionalProperties;
        this.resourceServerId = resourceServerId;
        this.isAllocated = isAllocated;
        this.purpose = purpose;
        this.status = status;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.updatedBy = updatedBy;
        this.updatedDate = updatedDate;
        this.resourceServer = resourceServer;
    }

    public ResourceItemDto() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getResourceServerId() {
        return resourceServerId;
    }

    public void setResourceServerId(String resourceServerId) {
        this.resourceServerId = resourceServerId;
    }

    public Boolean getIsAllocated() {
        return isAllocated;
    }

    public void setIsAllocated(Boolean isAllocated) {
        this.isAllocated = isAllocated;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public ResourceServerDto getResourceServer() {
        return resourceServer;
    }

    public void setResourceServer(ResourceServerDto resourceServer) {
        this.resourceServer = resourceServer;
    }

}
