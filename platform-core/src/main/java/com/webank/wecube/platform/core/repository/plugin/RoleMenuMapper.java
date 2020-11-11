package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.webank.wecube.platform.core.entity.plugin.RoleMenu;

public interface RoleMenuMapper {
    int deleteByPrimaryKey(String id);

    int insert(RoleMenu record);

    int insertSelective(RoleMenu record);

    RoleMenu selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(RoleMenu record);

    int updateByPrimaryKey(RoleMenu record);
    
    /**
     * 
     * @param roleName
     * @return
     */
    List<RoleMenu> selectAllByRoleName(@Param("roleName") String roleName);
    
    /**
     * 
     * @param roleName
     * @param menuCode
     * @return
     */
    List<RoleMenu> selectAllByRoleNameAndMenuCode(@Param("roleName")String roleName, @Param("menuCode")String menuCode);
}