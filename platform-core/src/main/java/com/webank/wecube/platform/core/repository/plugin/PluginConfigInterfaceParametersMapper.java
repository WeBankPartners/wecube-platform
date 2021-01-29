package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaceParameters;

@Repository
public interface PluginConfigInterfaceParametersMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginConfigInterfaceParameters record);

    int insertSelective(PluginConfigInterfaceParameters record);

    PluginConfigInterfaceParameters selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginConfigInterfaceParameters record);

    int updateByPrimaryKey(PluginConfigInterfaceParameters record);

    /**
     * 
     * @param pluginConfigInterfaceId
     * @param paramType
     * @return
     */
    List<PluginConfigInterfaceParameters> selectAllByConfigInterfaceAndParamType(
            @Param("pluginConfigInterfaceId") String pluginConfigInterfaceId, @Param("paramType")String paramType);
    
    /**
     * 
     * @param pluginConfigInterfaceId
     * @return
     */
    int deleteAllByConfigInterface(@Param("pluginConfigInterfaceId") String pluginConfigInterfaceId);
}