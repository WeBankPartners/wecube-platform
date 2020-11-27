package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.FavoritesRole;

public interface FavoritesRoleMapper {
    int deleteByPrimaryKey(String id);

    int insert(FavoritesRole record);

    int insertSelective(FavoritesRole record);

    FavoritesRole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FavoritesRole record);

    int updateByPrimaryKey(FavoritesRole record);
}