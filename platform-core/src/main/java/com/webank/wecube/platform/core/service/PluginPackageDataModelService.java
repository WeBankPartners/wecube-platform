package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;

import java.util.List;

public interface PluginPackageDataModelService {

    PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto);

    List<PluginPackageEntityDto> overview();

    List<PluginPackageEntityDto> packageView(String packageName);

    PluginPackageDataModelDto pullDynamicDataModel(String packageName);

}
