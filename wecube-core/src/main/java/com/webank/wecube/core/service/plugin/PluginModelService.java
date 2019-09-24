package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;

import java.util.EnumMap;
import java.util.List;

public interface PluginModelService {

    List<PluginModelEntityDto> register(List<PluginModelEntityDto> pluginModelEntityDtos);

    List<PluginModelEntityDto> update(List<PluginModelEntityDto> pluginModelEntityDtos);

    List<PluginModelEntityDto> overview();

    List<PluginModelEntityDto> overview(String packageName, String... version);

    void deleteEntity(String packageName, String... entityNames);


}
