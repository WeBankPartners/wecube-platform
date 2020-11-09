package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;

public interface PluginConfigInterfaceParametersMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginConfigInterfaceParameters record);

    int insertSelective(PluginConfigInterfaceParameters record);

    PluginConfigInterfaceParameters selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginConfigInterfaceParameters record);

    int updateByPrimaryKey(PluginConfigInterfaceParameters record);
}