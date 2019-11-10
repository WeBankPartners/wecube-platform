package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;

import java.util.List;
import java.util.Set;

public interface PluginPackageDataModelService {

    PluginPackageDataModelDto register(PluginPackageDataModelDto pluginPackageDataModelDto);

    List<PluginPackageEntityDto> overview();

    Set<PluginPackageDataModelDto> allDataModels();

    PluginPackageDataModelDto dataModelByPackageName(String packageName);

    List<PluginPackageEntityDto> packageView(String packageName);

    PluginPackageDataModelDto pullDynamicDataModel(String packageName);

}
