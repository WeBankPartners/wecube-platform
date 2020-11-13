package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigRoles;

@Repository
public interface PluginConfigRolesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginConfigRoles record);

    int insertSelective(PluginConfigRoles record);

    PluginConfigRoles selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginConfigRoles record);

    int updateByPrimaryKey(PluginConfigRoles record);
    
    /**
     * 
     * @param pluginConfigId
     * @return
     */
    List<PluginConfigRoles> selectAllByPluginConfig(@Param("pluginConfigId") String pluginConfigId);
}