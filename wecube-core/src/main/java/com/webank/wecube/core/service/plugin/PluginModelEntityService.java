package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.dto.PluginModelEntityDto;

import java.util.List;

public interface PluginModelEntityService {
    List<PluginModelEntityDto> register(List<PluginModelEntityDto> pluginModelEntityDtos);

    List<PluginModelEntityDto> update(List<PluginModelEntityDto> pluginModelEntityDtos);

    List<PluginModelEntityDto> overview();
}
