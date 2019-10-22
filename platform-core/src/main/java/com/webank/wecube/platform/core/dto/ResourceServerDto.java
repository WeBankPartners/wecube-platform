package com.webank.wecube.platform.core.dto;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceServer;
import com.webank.wecube.platform.core.interceptor.UsernameStorage;
import com.webank.wecube.platform.core.service.resource.ResourceAvaliableStatus;
import com.webank.wecube.platform.core.service.resource.ResourceServerType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ResourceServerDto {
    private Integer id;
    private String name;
    private String host;
    private String port;
    private String loginUsername;
    private String loginPassword;
    private String type;
    private Boolean isAllocated;
    private String purpose;
    private String status;
    @JsonIgnore
    private List<ResourceItemDto> resourceItemDtos;

    public static ResourceServerDto fromDomain(ResourceServer resourceServer) {
        ResourceServerDto resourceServerDto = new ResourceServerDto();
        resourceServerDto.setId(resourceServer.getId());
        resourceServerDto.setName(resourceServer.getName());
        resourceServerDto.setHost(resourceServer.getHost());
        resourceServerDto.setPort(resourceServer.getPort());
        resourceServerDto.setLoginUsername(resourceServer.getLoginUsername());
        resourceServerDto.setLoginPassword(resourceServer.getLoginPassword());
        resourceServerDto.setType(resourceServer.getType());
        resourceServerDto.setIsAllocated(resourceServer.getIsAllocated() != null && resourceServer.getIsAllocated() == 1 ? true : false);
        resourceServerDto.setPurpose(resourceServer.getPurpose());
        resourceServerDto.setStatus(resourceServer.getStatus());
        if (resourceServer.getResourceItems() != null) {
            resourceServerDto.setResourceItemDtos(Lists.transform(resourceServer.getResourceItems(), x -> ResourceItemDto.fromDomain(x)));
        }
        return resourceServerDto;
    }

    public static ResourceServer toDomain(ResourceServerDto resourceServerDto, ResourceServer existedResourceServer) {
        ResourceServer resourceServer = existedResourceServer;
        if (resourceServer == null) {
            resourceServer = new ResourceServer();
        }

        if (resourceServerDto.getId() != null) {
            resourceServer.setId(resourceServerDto.getId());
        }

        if (resourceServerDto.getName() != null) {
            resourceServer.setName(resourceServerDto.getName());
        }

        if (resourceServerDto.getHost() != null) {
            resourceServer.setHost(resourceServerDto.getHost());
        }
        if (resourceServerDto.getPort() != null) {
            resourceServer.setPort(resourceServerDto.getPort());
        }

        if (resourceServerDto.getLoginUsername() != null) {
            resourceServer.setLoginUsername(resourceServerDto.getLoginUsername());
        }

        if (resourceServerDto.getLoginPassword() != null) {
            resourceServer.setLoginPassword(resourceServerDto.getLoginPassword());
        }

        if (resourceServerDto.getType() != null) {
            validateServerType(resourceServerDto.getType());
            resourceServer.setType(resourceServerDto.getType());
        }

        if (resourceServerDto.getIsAllocated() != null) {
            resourceServer.setIsAllocated(resourceServerDto.getIsAllocated() != null && resourceServerDto.getIsAllocated() ? 1 : 0);
        }

        if (resourceServerDto.getPurpose() != null) {
            resourceServer.setPurpose(resourceServerDto.getPurpose());
        }

        if (resourceServerDto.getStatus() != null) {
            validateItemStatus(resourceServerDto.getStatus());
            resourceServer.setStatus(resourceServerDto.getStatus());
        }

        updateSystemFieldsWithDefaultValues(resourceServer);

        return resourceServer;
    }

    private static void updateSystemFieldsWithDefaultValues(ResourceServer resourceServer) {
        if (resourceServer.getStatus() == null) {
            resourceServer.setStatus(ResourceAvaliableStatus.CREATED.getCode());
        }

        if (resourceServer.getCreatedBy() == null) {
            resourceServer.setCreatedBy(UsernameStorage.getIntance().get());
        }

        if (resourceServer.getCreatedDate() == null) {
            resourceServer.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        }

        resourceServer.setUpdatedBy(UsernameStorage.getIntance().get());
        resourceServer.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
    }

    private static void validateServerType(String serverType) {
        if (ResourceServerType.fromCode(serverType) == ResourceServerType.NONE) {
            throw new WecubeCoreException(String.format("Unsupported resource server type [%s].", serverType));
        }
    }

    private static void validateItemStatus(String status) {
        if (ResourceAvaliableStatus.fromCode(status) == ResourceAvaliableStatus.NONE) {
            throw new WecubeCoreException(String.format("Unsupported resource item status [%s].", status));
        }
    }
}
