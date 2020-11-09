package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesMysql;

public interface PluginPackageRuntimeResourcesMysqlMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageRuntimeResourcesMysql record);

    int insertSelective(PluginPackageRuntimeResourcesMysql record);

    PluginPackageRuntimeResourcesMysql selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageRuntimeResourcesMysql record);

    int updateByPrimaryKey(PluginPackageRuntimeResourcesMysql record);
}