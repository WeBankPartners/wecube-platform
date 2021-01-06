package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.Favorites;

@Repository
public interface FavoritesMapper {
    int deleteByPrimaryKey(String favoritesId);

    int insert(Favorites record);

    int insertSelective(Favorites record);

    Favorites selectByPrimaryKey(String favoritesId);

    int updateByPrimaryKeySelective(Favorites record);

    int updateByPrimaryKeyWithBLOBs(Favorites record);

    int updateByPrimaryKey(Favorites record);
    
    /**
     * 
     * @param collectionName
     * @return
     */
    List<Favorites> selectAllByCollectionName(@Param("collectionName") String collectionName);
}