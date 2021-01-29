package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesMysql;

@Repository
public interface PluginPackageRuntimeResourcesMysqlMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageRuntimeResourcesMysql record);

    int insertSelective(PluginPackageRuntimeResourcesMysql record);

    PluginPackageRuntimeResourcesMysql selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageRuntimeResourcesMysql record);

    int updateByPrimaryKey(PluginPackageRuntimeResourcesMysql record);
    
    /**
     * 
     * @param pluginPackageId
     * @return
     */
    List<PluginPackageRuntimeResourcesMysql> selectAllByPackage(@Param("pluginPackageId")String pluginPackageId);
}