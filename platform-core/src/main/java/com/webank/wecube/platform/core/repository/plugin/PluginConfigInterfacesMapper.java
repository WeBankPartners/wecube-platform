package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginConfigInterfaces;
import com.webank.wecube.platform.core.entity.plugin.RichPluginConfigInterfaces;

/**
 * 
 * @author gavin
 *
 */
@Repository
public interface PluginConfigInterfacesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginConfigInterfaces record);

    int insertSelective(PluginConfigInterfaces record);

    PluginConfigInterfaces selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginConfigInterfaces record);

    int updateByPrimaryKey(PluginConfigInterfaces record);

    /**
     * 
     * @param pluginConfigId
     * @return
     */
    List<PluginConfigInterfaces> selectAllByPluginConfig(@Param("pluginConfigId") String pluginConfigId);

    /**
     * 
     * @param serviceName
     * @param configStatus
     * @return
     */
    List<RichPluginConfigInterfaces> selectAllByServiceNameAndConfigStatus(@Param("serviceName") String serviceName,
            @Param("configStatus") String configStatus);
}