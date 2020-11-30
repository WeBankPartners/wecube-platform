package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.FavoritesRole;

@Repository
public interface FavoritesRoleMapper {
    int deleteByPrimaryKey(String id);

    int insert(FavoritesRole record);

    int insertSelective(FavoritesRole record);

    FavoritesRole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FavoritesRole record);

    int updateByPrimaryKey(FavoritesRole record);

    /**
     * 
     * @param favoritesId
     * @param permission
     * @return
     */
    List<FavoritesRole> selectAllByFavoritesAndPermission(@Param("favoritesId") String favoritesId,
            @Param("permission") String permission);

    /**
     * 
     * @param favoritesId
     * @return
     */
    int deleteAllByFavorites(@Param("favoritesId") String favoritesId);

    /**
     * 
     * @param roleNames
     * @return
     */
    List<FavoritesRole> selectAllByRoles(@Param("roleNames") List<String> roleNames);

    /**
     * 
     * @param favoritesId
     * @return
     */
    List<FavoritesRole> selectAllByFavorites(@Param("favoritesId") String favoritesId);

    int deleteByfavoritesIdAndRoleIdAndPermission(@Param("favoritesId") String favoritesId,
            @Param("roleId") String roleId, @Param("permission") String permission);
}