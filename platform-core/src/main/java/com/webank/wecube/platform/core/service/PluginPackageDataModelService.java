package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;

import java.util.EnumMap;
import java.util.List;

public interface PluginPackageDataModelService {

    List<PluginPackageEntityDto> register(List<PluginPackageEntityDto> pluginModelEntityDtos);

    List<PluginPackageEntityDto> overview();

//    List<PluginPackageEntityDto> packageView(String packageName, String version);

    List<PluginPackageEntityDto> packageView(int packageId);

//    void deleteModel(String packageName, String version);

}
