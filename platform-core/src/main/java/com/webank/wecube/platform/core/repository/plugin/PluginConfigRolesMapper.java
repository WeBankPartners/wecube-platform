package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;

public interface PluginConfigRolesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginConfigRoles record);

    int insertSelective(PluginConfigRoles record);

    PluginConfigRoles selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginConfigRoles record);

    int updateByPrimaryKey(PluginConfigRoles record);
}