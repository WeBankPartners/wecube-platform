package com.webank.wecube.platform.core.repository.plugin;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageDataModel;

@Repository
public interface PluginPackageDataModelMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageDataModel record);

    int insertSelective(PluginPackageDataModel record);

    PluginPackageDataModel selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageDataModel record);

    int updateByPrimaryKey(PluginPackageDataModel record);
    
    
    /**
     * 
     * @param packageName
     * @return
     */
    PluginPackageDataModel selectLatestDataModelByPackageName(@Param("packageName") String packageName);
}