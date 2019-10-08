package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.jpa.PluginPackageRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginModelEntityDto {
    private String packageName;
    private String name;
    private String displayName;
    private String description;
    private String state = "draft";
    // plugin model attribute list
    private String packageVersion;
    private List<PluginModelAttributeDto> attributeDtoList = new ArrayList<>();

    public PluginModelEntityDto(String name, String displayName, String description, String state, List<PluginModelAttributeDto> attributeDtoList) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.state = state;
        this.attributeDtoList = attributeDtoList;
    }

    public PluginModelEntityDto() {
    }

    /**
     * @param pluginModelEntity input entity domain object
     * @return entity dto exposed to the server
     */
    public static PluginModelEntityDto fromDomain(PluginModelEntity pluginModelEntity) {
        PluginModelEntityDto pluginModelEntityDto = new PluginModelEntityDto();
        pluginModelEntityDto.setPackageName(pluginModelEntity.getPluginPackage().getName());
        pluginModelEntityDto.setName(pluginModelEntity.getName());
        pluginModelEntityDto.setDisplayName(pluginModelEntity.getDisplayName());
        pluginModelEntityDto.setDescription(pluginModelEntity.getDescription());
        pluginModelEntityDto.setState(pluginModelEntity.getState());
        pluginModelEntityDto.setPackageVersion(pluginModelEntity.getPluginPackage().getVersion());
        if (pluginModelEntity.getPluginModelAttributeList() != null) {
            pluginModelEntity.getPluginModelAttributeList()
                    .forEach(pluginModelAttribute -> pluginModelEntityDto.attributeDtoList
                            .add(PluginModelAttributeDto.fromDomain(pluginModelAttribute)));
        }
        return pluginModelEntityDto;
    }

    /**
     * @param pluginModelEntityDto input entity dto
     * @return transformed entity domain object
     */
    public static PluginModelEntity toDomain(PluginModelEntityDto pluginModelEntityDto) {

        PluginModelEntity pluginModelEntity = new PluginModelEntity();
        if (pluginModelEntityDto.getName() != null) {
            pluginModelEntity.setName(pluginModelEntityDto.getName());
        }

        if (pluginModelEntityDto.getDescription() != null) {
            pluginModelEntity.setDescription(pluginModelEntityDto.getDescription());
        }
        if (pluginModelEntityDto.getState() != null) {
            pluginModelEntity.setState(pluginModelEntityDto.getState().toLowerCase());
        }
        if (pluginModelEntityDto.getDisplayName() != null) {
            pluginModelEntity.setDisplayName(pluginModelEntityDto.getDisplayName());
        }
        if (pluginModelEntityDto.getAttributeDtoList() != null) {
            List<PluginModelAttribute> pluginModelAttributeList = new ArrayList<>();
            for (PluginModelAttributeDto pluginModelAttributeDto : pluginModelEntityDto.getAttributeDtoList()) {
                pluginModelAttributeList.add(PluginModelAttributeDto.toDomain(pluginModelAttributeDto, null, pluginModelEntity));
            }
            pluginModelEntity.setPluginModelAttributeList(pluginModelAttributeList);
        }
        return pluginModelEntity;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public List<PluginModelAttributeDto> getAttributeDtoList() {
        return attributeDtoList;
    }

    public void setAttributeDtoList(List<PluginModelAttributeDto> attributeDtoList) {
        this.attributeDtoList = attributeDtoList;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
