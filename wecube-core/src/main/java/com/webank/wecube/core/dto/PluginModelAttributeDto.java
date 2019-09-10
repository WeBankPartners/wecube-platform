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
  private PluginModelEntity pluginModelEntity;  // entityID in db as foreign key
  private String description;
  private String name;
  private String inputType;
  private PluginModelAttribute pluginModelAttribute;  // referenceID in db as foreign key

  public static PluginModelAttributeDto fromDomain(PluginModelAttribute pluginModelAttribute) {
    PluginModelAttributeDto pluginModelAttributeDto = new PluginModelAttributeDto();

    pluginModelAttributeDto.setId(pluginModelAttribute.getId());
    pluginModelAttributeDto.setPluginModelEntity(pluginModelAttribute.getPluginModelEntity());
    pluginModelAttributeDto.setDescription(pluginModelAttribute.getDescription());
    pluginModelAttributeDto.setName(pluginModelAttribute.getName());
    pluginModelAttributeDto.setInputType(pluginModelAttribute.getInputType());
    pluginModelAttributeDto.setPluginModelAttribute(pluginModelAttribute.getPluginModelAttribute());
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

    if (pluginModelAttributeDto.getPluginModelEntity() != null) {
      pluginModelAttribute.setPluginModelEntity(pluginModelAttributeDto.getPluginModelEntity());
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
