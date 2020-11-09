package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageAuthorities;

public interface PluginPackageAuthoritiesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageAuthorities record);

    int insertSelective(PluginPackageAuthorities record);

    PluginPackageAuthorities selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageAuthorities record);

    int updateByPrimaryKey(PluginPackageAuthorities record);
}