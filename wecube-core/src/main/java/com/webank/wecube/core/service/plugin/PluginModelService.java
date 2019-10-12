package com.webank.wecube.core.service.plugin;

import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;

import java.util.EnumMap;
import java.util.List;

public interface PluginModelService {

    List<PluginModelEntityDto> register(List<PluginModelEntityDto> pluginModelEntityDtos);

    List<PluginModelEntityDto> overview();

    List<PluginModelEntityDto> packageView(String packageName, String version);

    List<PluginModelEntityDto> packageView(int packageId);

    void deleteModel(String packageName, String version);

}
