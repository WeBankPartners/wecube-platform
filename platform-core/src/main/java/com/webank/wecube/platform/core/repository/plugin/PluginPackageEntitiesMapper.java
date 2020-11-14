package com.webank.wecube.platform.core.repository.plugin;

import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;

@Repository
public interface PluginPackageEntitiesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageEntities record);

    int insertSelective(PluginPackageEntities record);

    PluginPackageEntities selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageEntities record);

    int updateByPrimaryKey(PluginPackageEntities record);
}