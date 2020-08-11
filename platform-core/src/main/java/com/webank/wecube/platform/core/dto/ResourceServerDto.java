package com.webank.wecube.platform.core.dto;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.ResourceServer;
import com.webank.wecube.platform.core.service.resource.ResourceItemStatus;
import com.webank.wecube.platform.core.service.resource.ResourceServerStatus;
import com.webank.wecube.platform.core.service.resource.ResourceServerType;

@JsonInclude(Include.NON_NULL)
public class ResourceServerDto {
    private String id;
    private String name;
    private String host;
    private String port;
    private String loginUsername;
    private String loginPassword;
    private String type;
    private Boolean isAllocated;
    private String purpose;
    private String status;
    private String createdBy;
    private long createdDate;
    private String updatedBy;
    private long updatedDate;
    @JsonIgnore
    private List<ResourceItemDto> resourceItemDtos;

    public ResourceServerDto() {
        super();
    }

    public ResourceServerDto(String id, String name, String host, String port, String loginUsername,
            String loginPassword, String type, Boolean isAllocated, String purpose, String status, String createdBy,
            long createdDate, String updatedBy, long updatedDate, List<ResourceItemDto> resourceItemDtos) {
        super();
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.loginUsername = loginUsername;
        this.loginPassword = loginPassword;
        this.type = type;
        this.isAllocated = isAllocated;
        this.purpose = purpose;
        this.status = status;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.updatedBy = updatedBy;
        this.updatedDate = updatedDate;
        this.resourceItemDtos = resourceItemDtos;
    }

    public static ResourceServerDto fromDomain(ResourceServer resourceServer) {
        ResourceServerDto resourceServerDto = new ResourceServerDto();
        resourceServerDto.setId(resourceServer.getId());
        resourceServerDto.setName(resourceServer.getName());
        resourceServerDto.setHost(resourceServer.getHost());
        resourceServerDto.setPort(resourceServer.getPort());
        resourceServerDto.setLoginUsername(resourceServer.getLoginUsername());
        resourceServerDto.setLoginPassword(resourceServer.getLoginPassword());
        resourceServerDto.setType(resourceServer.getType());
        resourceServerDto.setIsAllocated(
                resourceServer.getIsAllocated() != null && resourceServer.getIsAllocated() == 1 ? true : false);
        resourceServerDto.setPurpose(resourceServer.getPurpose());
        resourceServerDto.setStatus(resourceServer.getStatus());
        resourceServerDto.setCreatedBy(resourceServer.getCreatedBy());
        resourceServerDto.setCreatedDate(resourceServer.getCreatedDate().getTime());
        resourceServerDto.setUpdatedBy(resourceServer.getUpdatedBy());
        resourceServerDto.setUpdatedDate(resourceServer.getUpdatedDate().getTime());
        if (resourceServer.getResourceItems() != null) {
            resourceServerDto.setResourceItemDtos(
                    Lists.transform(resourceServer.getResourceItems(), x -> ResourceItemDto.fromDomain(x)));
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
            resourceServer.setIsAllocated(
                    resourceServerDto.getIsAllocated() != null && resourceServerDto.getIsAllocated() ? 1 : 0);
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
            resourceServer.setStatus(ResourceItemStatus.CREATED.getCode());
        }

        if (resourceServer.getCreatedBy() == null) {
            resourceServer.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());
        }

        if (resourceServer.getCreatedDate() == null) {
            resourceServer.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        }

        resourceServer.setUpdatedBy(AuthenticationContextHolder.getCurrentUsername());
        resourceServer.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
    }

    private static void validateServerType(String serverType) {
        if (ResourceServerType.fromCode(serverType) == ResourceServerType.NONE) {
            String errMsg = String.format("Unsupported resource server type [%s].", serverType);
            throw new WecubeCoreException("3284", errMsg, serverType);
        }
    }

    private static void validateItemStatus(String status) {
        if (ResourceServerStatus.fromCode(status) == ResourceServerStatus.NONE) {
            String errMsg = String.format("Unsupported resource server status [%s].", status);
            throw new WecubeCoreException("3285",errMsg, status);
        }
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public List<ResourceItemDto> getResourceItemDtos() {
        return resourceItemDtos;
    }

    public void setResourceItemDtos(List<ResourceItemDto> resourceItemDtos) {
        this.resourceItemDtos = resourceItemDtos;
    }
}
