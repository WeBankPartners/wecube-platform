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
    private Integer referenceId;
    // plugin model entity info
    private Integer pluginModelEntityId;

    public static PluginModelAttributeDto fromDomain(PluginModelAttribute pluginModelAttribute) {
        PluginModelAttributeDto pluginModelAttributeDto = new PluginModelAttributeDto();

        pluginModelAttributeDto.setId(pluginModelAttribute.getId());
        pluginModelAttributeDto.setDescription(pluginModelAttribute.getDescription());
        pluginModelAttributeDto.setName(pluginModelAttribute.getName());
        pluginModelAttributeDto.setInputType(pluginModelAttribute.getInputType());
        pluginModelAttributeDto.setReferenceId(pluginModelAttribute.getPluginModelAttribute().getId());
        pluginModelAttributeDto.setPluginModelEntityId(pluginModelAttribute.getPluginModelEntityId());
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

        if (pluginModelAttributeDto.getDescription() != null) {
            pluginModelAttribute.setDescription(pluginModelAttributeDto.getDescription());
        }

        if (pluginModelAttributeDto.getName() != null) {
            pluginModelAttribute.setName(pluginModelAttributeDto.getName());
        }

        if (pluginModelAttributeDto.getInputType() != null) {
            pluginModelAttribute.setInputType(pluginModelAttributeDto.getInputType());
        }

        if (pluginModelAttributeDto.getReferenceId() != null) {
            pluginModelAttribute
                    .setReferenceId(pluginModelAttributeDto.getReferenceId());
        }

        return pluginModelAttribute;
    }
}
