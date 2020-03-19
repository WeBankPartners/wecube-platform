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
    private String targetEntityWithFilterRule;
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

    public String getTargetEntityWithFilterRule() {
        return targetEntityWithFilterRule;
    }

    public void setTargetEntityWithFilterRule(String targetEntityWithFilterRule) {
        this.targetEntityWithFilterRule = targetEntityWithFilterRule;
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

    public PluginConfigDto(String id, String pluginPackageId, String name, String targetEntityWithFilterRule,
            String status, List<PluginConfigInterfaceDto> interfaces) {
        this.id = id;
        this.pluginPackageId = pluginPackageId;
        this.name = name;
        this.targetEntityWithFilterRule = targetEntityWithFilterRule;
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

        if (StringUtils.isNotBlank(getFilterRule())) {
            pluginConfig.setTargetEntityFilterRule(getFilterRule());
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
        pluginConfigDto.setTargetEntityWithFilterRule(pluginConfig.getTargetEntityWithFilterRule());
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
        pluginConfigDto.setTargetEntityWithFilterRule(pluginConfig.getTargetEntityWithFilterRule());
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

    public String getTargetPackage() {
        return splitTargetEntityWithFilterRule().getTargetPackage();
    }

    public String getTargetEntity() {
        return splitTargetEntityWithFilterRule().getTargetEntity();
    }

    public String getFilterRule() {
        return splitTargetEntityWithFilterRule().getFilterRule();
    }

    public class TargetEntityWithFilterRule {
        private String targetPackage;
        private String targetEntity;
        private String FilterRule;

        public String getTargetPackage() {
            return targetPackage;
        }

        public String getTargetEntity() {
            return targetEntity;
        }

        public String getFilterRule() {
            return FilterRule;
        }

        public TargetEntityWithFilterRule(String targetPackage, String targetEntity, String filterRule) {
            super();
            this.targetPackage = targetPackage;
            this.targetEntity = targetEntity;
            FilterRule = filterRule;
        }

        public TargetEntityWithFilterRule() {
            super();
        }
    }

    public TargetEntityWithFilterRule splitTargetEntityWithFilterRule() {
        String targetEntityWithFilterRule = getTargetEntityWithFilterRule();
        String packageString = "";
        String entity = "";
        String filterRule = "";
        int index1 = StringUtils.indexOf(targetEntityWithFilterRule, ":");
        if (index1 != -1) {
            packageString = StringUtils.substring(targetEntityWithFilterRule, 0, index1);
            int index2 = StringUtils.indexOf(targetEntityWithFilterRule, "{");

            if (index2 != -1) {
                entity = StringUtils.substring(targetEntityWithFilterRule, index1 + 1, index2);
                filterRule = StringUtils.substring(targetEntityWithFilterRule, index2);
            } else {
                entity = StringUtils.substring(targetEntityWithFilterRule, index1 + 1);
            }
        } else {
            packageString = targetEntityWithFilterRule;
        }

        return new TargetEntityWithFilterRule(packageString, entity, filterRule);
    }

}
