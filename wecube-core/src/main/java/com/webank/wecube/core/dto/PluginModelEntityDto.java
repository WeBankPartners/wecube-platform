package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginModelEntityDto {
    private Integer id;
    private Integer packageId;
    private String description;
    private String name;
    // plugin model attribute list
    private List<PluginModelAttributeDto> attributeDtoList;

    public static PluginModelEntityDto fromDomain(PluginModelEntity pluginModelEntity) {
        PluginModelEntityDto pluginModelEntityDto = new PluginModelEntityDto();
        pluginModelEntityDto.setId(pluginModelEntity.getId());
        pluginModelEntityDto.setPackageId(pluginModelEntity.getPluginPackage().getId());
        pluginModelEntityDto.setDescription(pluginModelEntity.getDescription());
        pluginModelEntityDto.setName(pluginModelEntity.getName());
        if (pluginModelEntity.getPluginModelAttributeList() != null) {
            pluginModelEntity.getPluginModelAttributeList()
                    .forEach(pluginModelAttribute -> pluginModelEntityDto.attributeDtoList
                            .add(PluginModelAttributeDto.fromDomain(pluginModelAttribute)));
        }
        return pluginModelEntityDto;
    }

    public static PluginModelEntity toDomain(PluginModelEntityDto pluginModelEntityDto,
                                             PluginModelEntity existedPluginModelEnitty) {
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
