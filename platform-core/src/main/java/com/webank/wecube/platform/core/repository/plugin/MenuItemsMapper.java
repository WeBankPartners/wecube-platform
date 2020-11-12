package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.MenuItems;

@Repository
public interface MenuItemsMapper {
    int deleteByPrimaryKey(String id);

    int insert(MenuItems record);

    int insertSelective(MenuItems record);

    MenuItems selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(MenuItems record);

    int updateByPrimaryKey(MenuItems record);
    
    MenuItems selectByMenuCode(@Param("code")String code);
    
    List<MenuItems> selectAll();
    
    List<MenuItems> selectAllRootMenuItems();
}