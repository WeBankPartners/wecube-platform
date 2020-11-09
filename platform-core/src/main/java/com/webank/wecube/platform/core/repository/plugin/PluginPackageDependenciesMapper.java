package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageDependencies;

public interface PluginPackageDependenciesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageDependencies record);

    int insertSelective(PluginPackageDependencies record);

    PluginPackageDependencies selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageDependencies record);

    int updateByPrimaryKey(PluginPackageDependencies record);
}