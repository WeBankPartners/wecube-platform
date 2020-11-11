package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageDependencies;

@Repository
public interface PluginPackageDependenciesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageDependencies record);

    int insertSelective(PluginPackageDependencies record);

    PluginPackageDependencies selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageDependencies record);

    int updateByPrimaryKey(PluginPackageDependencies record);
    
    List<PluginPackageDependencies> selectAllByPackage(@Param("pluginPackageId") String pluginPackageId);
}