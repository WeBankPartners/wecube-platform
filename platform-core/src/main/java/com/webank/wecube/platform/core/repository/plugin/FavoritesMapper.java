package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.Favorites;

public interface FavoritesMapper {
    int deleteByPrimaryKey(String favoritesId);

    int insert(Favorites record);

    int insertSelective(Favorites record);

    Favorites selectByPrimaryKey(String favoritesId);

    int updateByPrimaryKeySelective(Favorites record);

    int updateByPrimaryKeyWithBLOBs(Favorites record);

    int updateByPrimaryKey(Favorites record);
}