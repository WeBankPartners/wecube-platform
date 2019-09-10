package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.dto.PluginModelAttributeDto;

import java.util.List;

public interface PluginModelAttributeService {
    List<PluginModelAttributeDto> register(List<PluginModelAttributeDto> pluginModelAttributeDtos);

    List<PluginModelAttributeDto> update(List<PluginModelAttributeDto> pluginModelAttributeDtos);

    List<PluginModelAttributeDto> overview();
}
