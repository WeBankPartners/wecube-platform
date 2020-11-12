package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigs;

@Repository
public interface PluginConfigsMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginConfigs record);

    int insertSelective(PluginConfigs record);

    PluginConfigs selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginConfigs record);

    int updateByPrimaryKey(PluginConfigs record);

    /**
     * 
     * @param pluginPackageId
     * @return
     */
    List<PluginConfigs> selectAllByPackageAndOrderByConfigName(@Param("pluginPackageId") String pluginPackageId);
}