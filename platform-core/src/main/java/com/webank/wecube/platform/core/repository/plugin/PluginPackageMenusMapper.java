package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageMenus;

public interface PluginPackageMenusMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageMenus record);

    int insertSelective(PluginPackageMenus record);

    PluginPackageMenus selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageMenus record);

    int updateByPrimaryKey(PluginPackageMenus record);
    
    List<PluginPackageMenus> findAllActiveMenuByCode(String code, List<String> activePackageStatuses);
}