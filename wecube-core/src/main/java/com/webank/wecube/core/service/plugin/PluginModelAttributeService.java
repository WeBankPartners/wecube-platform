package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.dto.PluginModelAttributeDto;

import java.util.List;

public interface PluginModelAttributeService {
    List<PluginModelAttributeDto> registerPluginModelAttribute(List<PluginModelAttributeDto> pluginModelAttributeDtos);
    List<PluginModelAttributeDto> updatePluginModelAttribute(List<PluginModelAttributeDto> pluginModelAttributeDtos);
}
