package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;

public interface PluginConfigInterfacesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginConfigInterfaces record);

    int insertSelective(PluginConfigInterfaces record);

    PluginConfigInterfaces selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginConfigInterfaces record);

    int updateByPrimaryKey(PluginConfigInterfaces record);
}