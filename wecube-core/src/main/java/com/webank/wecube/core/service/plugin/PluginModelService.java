package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;

import java.util.EnumMap;
import java.util.List;

public interface PluginModelService {
    // plugin model enum for multiple return result usage
    enum PluginModel {
        entityDtos, attributeDtos
    }

    EnumMap<PluginModel, Object> register(List<PluginModelEntityDto> pluginModelEntityDtos, List<PluginModelAttributeDto> pluginModelAttributeDtos);

    EnumMap<PluginModel, Object> update(List<PluginModelEntityDto> pluginModelEntityDtos, List<PluginModelAttributeDto> pluginModelAttributeDtos);

    EnumMap<PluginModel, Object> overview();

}
