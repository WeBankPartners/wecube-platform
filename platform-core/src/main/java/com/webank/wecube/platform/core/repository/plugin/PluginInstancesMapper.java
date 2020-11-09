package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginInstances;

public interface PluginInstancesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginInstances record);

    int insertSelective(PluginInstances record);

    PluginInstances selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginInstances record);

    int updateByPrimaryKey(PluginInstances record);
}