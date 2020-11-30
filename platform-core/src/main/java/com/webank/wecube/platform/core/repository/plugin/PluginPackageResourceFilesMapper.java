package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageResourceFiles;

@Repository
public interface PluginPackageResourceFilesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageResourceFiles record);

    int insertSelective(PluginPackageResourceFiles record);

    PluginPackageResourceFiles selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageResourceFiles record);

    int updateByPrimaryKey(PluginPackageResourceFiles record);

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    List<PluginPackageResourceFiles> selectAllByPluginPackage(@Param("pluginPackageId") String pluginPackageId);
}