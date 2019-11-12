package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;

import java.util.Set;

public interface PluginPackageDataModelService {

    PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto);

    PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto, boolean isDynamic);

    Set<PluginPackageDataModelDto> overview();

    PluginPackageDataModelDto packageView(String packageName);

    PluginPackageDataModelDto pullDynamicDataModel(String packageName);

}
