package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.RolePluginInterface;

public interface RolePluginInterfaceMapper {
    int deleteByPrimaryKey(String id);

    int insert(RolePluginInterface record);

    int insertSelective(RolePluginInterface record);

    RolePluginInterface selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(RolePluginInterface record);

    int updateByPrimaryKey(RolePluginInterface record);
}