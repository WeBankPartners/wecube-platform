package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginInstances;

@Repository
public interface PluginInstancesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginInstances record);

    int insertSelective(PluginInstances record);

    PluginInstances selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginInstances record);

    int updateByPrimaryKey(PluginInstances record);
    
    /**
     * 
     * @param pluginPackageId
     * @return
     */
    List<PluginInstances> selectAllByPluginPackage(@Param("pluginPackageId") String pluginPackageId);
}