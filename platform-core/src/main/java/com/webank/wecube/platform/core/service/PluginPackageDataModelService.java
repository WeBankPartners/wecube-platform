package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;

import java.util.EnumMap;
import java.util.List;

public interface PluginPackageDataModelService {

    List<PluginPackageEntityDto> register(List<PluginPackageEntityDto> pluginModelEntityDtos);

    PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto);

    List<PluginPackageEntityDto> overview();

//    List<PluginPackageEntityDto> packageView(String packageName, String version);

    List<PluginPackageEntityDto> packageView(int packageId);

    PluginPackageDataModelDto pullDynamicDataModel(int pluginPackageId);

//    void deleteModel(String packageName, String version);

}
