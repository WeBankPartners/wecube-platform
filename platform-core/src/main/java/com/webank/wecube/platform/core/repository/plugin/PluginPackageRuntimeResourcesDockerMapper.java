package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesDocker;

public interface PluginPackageRuntimeResourcesDockerMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageRuntimeResourcesDocker record);

    int insertSelective(PluginPackageRuntimeResourcesDocker record);

    PluginPackageRuntimeResourcesDocker selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageRuntimeResourcesDocker record);

    int updateByPrimaryKey(PluginPackageRuntimeResourcesDocker record);
}