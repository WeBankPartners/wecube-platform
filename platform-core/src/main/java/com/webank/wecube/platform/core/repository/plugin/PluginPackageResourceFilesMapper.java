package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageResourceFiles;

public interface PluginPackageResourceFilesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageResourceFiles record);

    int insertSelective(PluginPackageResourceFiles record);

    PluginPackageResourceFiles selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageResourceFiles record);

    int updateByPrimaryKey(PluginPackageResourceFiles record);
}