package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

public class PluginConfigDto {
    private String id;
    private String pluginPackageId;
    private String name;
    private String targetPackage;
    private String targetEntity;
    private String registerName;
    private String status;
    private List<PluginConfigInterfaceDto> interfaces;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPluginPackageId() {
        return pluginPackageId;
    }

    public void setPluginPackageId(String pluginPackageId) {
        this.pluginPackageId = pluginPackageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
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

    public PluginConfigDto(String id, String pluginPackageId, String name, String targetPackage, String targetEntity,
            String status, List<PluginConfigInterfaceDto> interfaces) {
        this.id = id;
        this.pluginPackageId = pluginPackageId;
        this.name = name;
        this.targetPackage = targetPackage;
        this.targetEntity = targetEntity;
        this.status = status;
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public PluginConfig toDomain(PluginPackage pluginPackage) {
        PluginConfig pluginConfig = new PluginConfig();
        if (getId() != null) {
            pluginConfig.setId(getId());
        }
        pluginConfig.setPluginPackage(pluginPackage);

        pluginConfig.setName(getName());
        if (StringUtils.isNotBlank(getTargetPackage())) {
            pluginConfig.setTargetPackage(getTargetPackage());
        } else {
            pluginConfig.setTargetPackage(pluginPackage.getName());
        }
        if (StringUtils.isNotBlank(getTargetEntity())) {
            pluginConfig.setTargetEntity(getTargetEntity());
        }
        pluginConfig.setRegisterName(getRegisterName());
        Set<PluginConfigInterface> pluginConfigInterfaces = newLinkedHashSet();
        if (null != getInterfaces() && getInterfaces().size() > 0) {
            getInterfaces().forEach(interfaceDto -> pluginConfigInterfaces.add(interfaceDto.toDomain(pluginConfig)));
        }
        pluginConfig.setInterfaces(pluginConfigInterfaces);

        return pluginConfig;
    }

    public static PluginConfigDto fromDomain(PluginConfig pluginConfig) {
        PluginConfigDto pluginConfigDto = new PluginConfigDto();
        pluginConfigDto.setId(pluginConfig.getId());
        pluginConfigDto.setName(pluginConfig.getName());
        pluginConfigDto.setTargetPackage(pluginConfig.getTargetPackage());
        pluginConfigDto.setTargetEntity(pluginConfig.getTargetEntity());
        pluginConfigDto.setRegisterName(pluginConfig.getRegisterName());
        pluginConfigDto.setPluginPackageId(pluginConfig.getPluginPackage().getId());
        pluginConfigDto.setStatus(pluginConfig.getStatus().name());
        List<PluginConfigInterfaceDto> interfaces = newArrayList();
        if (null != pluginConfig.getInterfaces() && pluginConfig.getInterfaces().size() > 0) {
            pluginConfig.getInterfaces().forEach(pluginConfigInterface -> interfaces
                    .add(PluginConfigInterfaceDto.fromDomain(pluginConfigInterface)));
        }
        pluginConfigDto.setInterfaces(interfaces);
        return pluginConfigDto;
    }

    public static PluginConfigDto fromDomainWithoutInterfaces(PluginConfig pluginConfig) {
        PluginConfigDto pluginConfigDto = new PluginConfigDto();
        pluginConfigDto.setId(pluginConfig.getId());
        pluginConfigDto.setName(pluginConfig.getName());
        pluginConfigDto.setTargetPackage(pluginConfig.getTargetPackage());
        pluginConfigDto.setTargetEntity(pluginConfig.getTargetEntity());
        pluginConfigDto.setRegisterName(pluginConfig.getRegisterName());
        pluginConfigDto.setPluginPackageId(pluginConfig.getPluginPackage().getId());
        pluginConfigDto.setStatus(pluginConfig.getStatus().name());
        return pluginConfigDto;
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }
}
