package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageRuntimeResourcesDocker;

@Repository
public interface PluginPackageRuntimeResourcesDockerMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageRuntimeResourcesDocker record);

    int insertSelective(PluginPackageRuntimeResourcesDocker record);

    PluginPackageRuntimeResourcesDocker selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageRuntimeResourcesDocker record);

    int updateByPrimaryKey(PluginPackageRuntimeResourcesDocker record);
    
    /**
     * 
     * @param pluginPackageId
     * @return
     */
    List<PluginPackageRuntimeResourcesDocker> selectAllByPackage(@Param("pluginPackageId")String pluginPackageId);
}