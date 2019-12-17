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
    private String packageName;
    private String entityName;
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

    public PluginConfigDto(String id, String pluginPackageId, String name, String packageName, String entityName, String status, List<PluginConfigInterfaceDto> interfaces) {
        this.id = id;
        this.pluginPackageId = pluginPackageId;
        this.name = name;
        this.packageName = packageName;
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
        if (getId() != null) {
            pluginConfig.setId(getId());
        }
        pluginConfig.setPluginPackage(pluginPackage);

        pluginConfig.setName(getName());
        pluginConfig.setPackageName(getPackageName());
        if (StringUtils.isNotBlank(getEntityName())) {
            pluginConfig.setEntityName(getEntityName());
        }
        pluginConfig.setRegisterName(getRegisterName());
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
        pluginConfigDto.setPackageName(pluginConfig.getPackageName());
        pluginConfigDto.setEntityName(pluginConfig.getEntityName());
        pluginConfigDto.setRegisterName(pluginConfig.getRegisterName());
        pluginConfigDto.setPluginPackageId(pluginConfig.getPluginPackage().getId());
        pluginConfigDto.setStatus(pluginConfig.getStatus().name());
        List<PluginConfigInterfaceDto> interfaces = newArrayList();
        if (null != pluginConfig.getInterfaces() && pluginConfig.getInterfaces().size() > 0) {
            pluginConfig.getInterfaces().forEach(pluginConfigInterface -> interfaces.add(PluginConfigInterfaceDto.fromDomain(pluginConfigInterface)));
        }
        pluginConfigDto.setInterfaces(interfaces);
        return pluginConfigDto;
    }

    public String getRegisterName() {
        return registerName;
    }

    public void setRegisterName(String registerName) {
        this.registerName = registerName;
    }
}
