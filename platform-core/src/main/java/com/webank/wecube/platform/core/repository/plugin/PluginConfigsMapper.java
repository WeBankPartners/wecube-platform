package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;

public interface PluginConfigsMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginConfigs record);

    int insertSelective(PluginConfigs record);

    PluginConfigs selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginConfigs record);

    int updateByPrimaryKey(PluginConfigs record);
}