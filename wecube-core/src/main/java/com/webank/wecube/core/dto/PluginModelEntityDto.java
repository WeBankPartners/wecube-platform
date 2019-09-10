package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.domain.plugin.PluginPackage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginModelEntityDto {
    private Integer id;
    private PluginPackage pluginPackage;  // packageID
    private String description;
    private String name;

    public static PluginModelEntityDto fromDomain(PluginModelEntity pluginModelEntity) {
        PluginModelEntityDto pluginModelEntityDto = new PluginModelEntityDto();
        pluginModelEntityDto.setId(pluginModelEntity.getId());
        pluginModelEntityDto.setPluginPackage(pluginModelEntity.getPluginPackage());
        pluginModelEntityDto.setDescription(pluginModelEntity.getDescription());
        pluginModelEntityDto.setName(pluginModelEntity.getName());

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

        if (pluginModelEntityDto.getPluginPackage() != null) {
            pluginModelEntity.setPluginPackage(pluginModelEntityDto.getPluginPackage());
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
