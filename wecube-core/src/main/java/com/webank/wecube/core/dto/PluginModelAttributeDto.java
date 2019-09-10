package com.webank.wecube.core.dto;

import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginModelAttributeDto {

    private Integer id;
    private String description;
    private String name;
    private String inputType;
    private PluginModelAttribute pluginModelAttribute;  // referenceID in db as foreign key
    // plugin model entity info
    private Integer pluginModelEntityId;
    private String pluginModelEntityName;
    private String pluginModelEntityDescription;

    public static PluginModelAttributeDto fromDomain(PluginModelAttribute pluginModelAttribute) {
        PluginModelAttributeDto pluginModelAttributeDto = new PluginModelAttributeDto();

        pluginModelAttributeDto.setId(pluginModelAttribute.getId());
        pluginModelAttributeDto.setPluginModelEntityId(pluginModelAttribute.getPluginModelEntityId());
        pluginModelAttributeDto.setDescription(pluginModelAttribute.getDescription());
        pluginModelAttributeDto.setName(pluginModelAttribute.getName());
        pluginModelAttributeDto.setInputType(pluginModelAttribute.getInputType());
        pluginModelAttributeDto.setPluginModelAttribute(pluginModelAttribute.getPluginModelAttribute());
        pluginModelAttributeDto.setPluginModelEntityName(pluginModelAttribute.getPluginModelEntity().getName());
        pluginModelAttributeDto.setPluginModelEntityDescription(pluginModelAttribute.getPluginModelEntity().getDescription());
        return pluginModelAttributeDto;
    }

    public static PluginModelAttribute toDomain(PluginModelAttributeDto pluginModelAttributeDto,
                                                PluginModelAttribute existedPluginModelAttribute) {
        PluginModelAttribute pluginModelAttribute = existedPluginModelAttribute;
        if (pluginModelAttribute == null) {
            pluginModelAttribute = new PluginModelAttribute();
        }

        if (pluginModelAttributeDto.getId() != null) {
            pluginModelAttribute.setId(pluginModelAttributeDto.getId());
        }

        if (pluginModelAttributeDto.getPluginModelEntityId() != null) {
            pluginModelAttribute.setPluginModelEntityId(pluginModelAttributeDto.getPluginModelEntityId());
        }

        if (pluginModelAttributeDto.getId() != null) {
            pluginModelAttribute.setDescription(pluginModelAttributeDto.getDescription());
        }

        if (pluginModelAttributeDto.getId() != null) {
            pluginModelAttribute.setName(pluginModelAttributeDto.getName());
        }

        if (pluginModelAttributeDto.getId() != null) {
            pluginModelAttribute.setInputType(pluginModelAttributeDto.getInputType());
        }

        if (pluginModelAttributeDto.getId() != null) {
            pluginModelAttribute
                    .setPluginModelAttribute(pluginModelAttributeDto.getPluginModelAttribute());
        }

        return pluginModelAttribute;
    }
}
