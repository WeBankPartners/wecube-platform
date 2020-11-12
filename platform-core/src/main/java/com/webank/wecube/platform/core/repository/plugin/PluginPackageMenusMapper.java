package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageMenus;

@Repository
public interface PluginPackageMenusMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageMenus record);

    int insertSelective(PluginPackageMenus record);

    PluginPackageMenus selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageMenus record);

    int updateByPrimaryKey(PluginPackageMenus record);
    
    /**
     * 
     * @param code
     * @param activePackageStatuses
     * @return
     */
    List<PluginPackageMenus> selectAllMenusByCodeAndPackageStatuses(@Param("code")String code, @Param("statuses")List<String> statuses);
    
    /**
     * 
     * @param statuses
     * @return
     */
    List<PluginPackageMenus> selectAllMenusByPackageStatuses( @Param("statuses")List<String> statuses);
}