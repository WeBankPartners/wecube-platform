package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

public class PluginConfigDto {
    private Integer id;
    private Integer pluginPackageId;
    private String name;
    private Integer entityId;
    private String entityName;
    private String status;
    private List<PluginConfigInterfaceDto> interfaces;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(Integer pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PluginConfigInterfaceDto> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<PluginConfigInterfaceDto> interfaces) {
        this.interfaces = interfaces;
    }

    public PluginConfigDto() {
    }

    public PluginConfigDto(Integer id, Integer pluginPackageId, String name, Integer entityId, String entityName, String status, List<PluginConfigInterfaceDto> interfaces) {
        this.id = id;
        this.pluginPackageId = pluginPackageId;
        this.name = name;
        this.entityId = entityId;
        this.entityName = entityName;
        this.status = status;
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public PluginConfig toDomain(PluginPackage pluginPackage) {
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setId(getId());
        pluginConfig.setPluginPackage(pluginPackage);

        pluginConfig.setName(getName());
        pluginConfig.setEntityId(getEntityId());
        pluginConfig.setEntityName(getEntityName());
        Set<PluginConfigInterface> pluginConfigInterfaces = newLinkedHashSet();
        if (null != getInterfaces() && getInterfaces().size() > 0) {
            getInterfaces().forEach(interfaceDto->pluginConfigInterfaces.add(interfaceDto.toDomain(pluginConfig)));
        }
        pluginConfig.setInterfaces(pluginConfigInterfaces);

        return pluginConfig;
    }

    public static PluginConfigDto fromDomain(PluginConfig pluginConfig) {
        PluginConfigDto pluginConfigDto = new PluginConfigDto();
        pluginConfigDto.setId(pluginConfig.getId());
        pluginConfigDto.setName(pluginConfig.getName());
        pluginConfigDto.setEntityId(pluginConfig.getEntityId());
        pluginConfigDto.setEntityName(pluginConfig.getEntityName());
        pluginConfigDto.setPluginPackageId(pluginConfig.getPluginPackage().getId());
        pluginConfigDto.setStatus(pluginConfig.getStatus().name());
        List<PluginConfigInterfaceDto> interfaces = newArrayList();
        if (null != pluginConfig.getInterfaces() && pluginConfig.getInterfaces().size() > 0) {
            pluginConfig.getInterfaces().forEach(pluginConfigInterface -> interfaces.add(PluginConfigInterfaceDto.fromDomain(pluginConfigInterface)));
        }
        pluginConfigDto.setInterfaces(interfaces);
        return pluginConfigDto;
    }
}
