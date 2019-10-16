package com.webank.wecube.platform.core.dto;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceItem;
import com.webank.wecube.platform.core.interceptor.UsernameStorage;
import com.webank.wecube.platform.core.service.resource.ResourceAvaliableStatus;
import com.webank.wecube.platform.core.service.resource.ResourceItemType;
import com.webank.wecube.platform.core.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ResourceItemDto {
    private Integer id;
    private String name;
    private String type;
    private String additionalProperties;
    private Integer resourceServerId;
    private Boolean isAllocated;
    private String purpose;
    private String status;
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
        resourceItemDto.setIsAllocated(resourceItem.getIsAllocated() != null && resourceItem.getIsAllocated() == 1 ? true : false);
        resourceItemDto.setPurpose(resourceItem.getPurpose());
        resourceItemDto.setStatus(resourceItem.getStatus());
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
            resourceItem.setIsAllocated(resourceItemDto.getIsAllocated() != null && resourceItemDto.getIsAllocated() ? 1 : 0);
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
            resourceItem.setStatus(ResourceAvaliableStatus.CREATED.getCode());
        }

        if (resourceItem.getCreatedBy() == null) {
            resourceItem.setCreatedBy(UsernameStorage.getIntance().get());
        }

        if (resourceItem.getCreatedDate() == null) {
            resourceItem.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        }

        resourceItem.setUpdatedBy(UsernameStorage.getIntance().get());
        resourceItem.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
    }

    private static void validateItemType(String itemType) {
        if (ResourceItemType.fromCode(itemType) == ResourceItemType.NONE) {
            throw new WecubeCoreException(String.format("Unsupported resource item type [%s].", itemType));
        }
    }

    private static void validateItemStatus(String status) {
        if (ResourceAvaliableStatus.fromCode(status) == ResourceAvaliableStatus.NONE) {
            throw new WecubeCoreException(String.format("Unsupported resource item status [%s].", status));
        }
    }

    public Map<String, String> getAdditionalPropertiesMap() {
        if (additionalProperties != null) {
            return convertToMap(additionalProperties);
        }
        return new HashMap<String, String>();
    }

    private Map<String, String> convertToMap(String additionalProperties) {
        try {
            return JsonUtils.toObject(additionalProperties, Map.class);
        } catch (IOException e) {
            throw new WecubeCoreException(String.format("Failed to parse resource_item.additional_properties [%s] : Invalid json format.", additionalProperties), e);
        }
    }
}
