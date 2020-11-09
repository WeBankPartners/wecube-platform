package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesS3;

public interface PluginPackageRuntimeResourcesS3Mapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageRuntimeResourcesS3 record);

    int insertSelective(PluginPackageRuntimeResourcesS3 record);

    PluginPackageRuntimeResourcesS3 selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageRuntimeResourcesS3 record);

    int updateByPrimaryKey(PluginPackageRuntimeResourcesS3 record);
}