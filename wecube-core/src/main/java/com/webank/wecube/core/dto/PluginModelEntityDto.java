package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginModelEntityDto {
    private Integer id;
    private String description;
    private String name;
    // plugin package info
    private Integer packageId;
    private String packageName;
    private String packageVersion;

    public static PluginModelEntityDto fromDomain(PluginModelEntity pluginModelEntity) {
        PluginModelEntityDto pluginModelEntityDto = new PluginModelEntityDto();
        pluginModelEntityDto.setId(pluginModelEntity.getId());
        pluginModelEntityDto.setPackageId(pluginModelEntity.getPluginPackage().getId());
        pluginModelEntityDto.setDescription(pluginModelEntity.getDescription());
        pluginModelEntityDto.setName(pluginModelEntity.getName());
        pluginModelEntityDto.setPackageName(pluginModelEntity.getPluginPackage().getName());
        pluginModelEntityDto.setPackageVersion(pluginModelEntity.getPluginPackage().getVersion());

        return pluginModelEntityDto;
    }

    public static PluginModelEntity toDomain(PluginModelEntityDto pluginModelEntityDto, PluginModelEntity existedPluginModelEnitty) {
        PluginModelEntity pluginModelEntity = existedPluginModelEnitty;
        if (pluginModelEntity == null) {
            pluginModelEntity = new PluginModelEntity();
        }

        if (pluginModelEntityDto.getId() != null) {
            pluginModelEntity.setId(pluginModelEntityDto.getId());
        }

        if (pluginModelEntityDto.getPackageId() != null) {
            pluginModelEntity.setPackageId(pluginModelEntityDto.getPackageId());
        }

        if (pluginModelEntityDto.getDescription() != null) {
            pluginModelEntity.setDescription(pluginModelEntityDto.getDescription());
        }

        if (pluginModelEntityDto.getName() != null) {
            pluginModelEntity.setName(pluginModelEntityDto.getName());
        }

        return pluginModelEntity;
    }
}
