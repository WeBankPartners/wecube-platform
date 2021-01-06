package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginInstances;
import com.webank.wecube.platform.core.entity.plugin.PluginInstancesInfo;

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

    /**
     * 
     * @param pluginPackageId
     * @param status
     * @return
     */
    List<PluginInstances> selectAllByPluginPackageAndStatus(@Param("pluginPackageId") String pluginPackageId,
            @Param("status") String status);

    /**
     * 
     * @param hostIpAddr
     * @param port
     * @param status
     * @return
     */
    List<PluginInstances> selectAllByHostAndPortAndStatus(@Param("hostIpAddr") String hostIpAddr,
            @Param("port") int port, @Param("status") String status);

    /**
     * 
     * @return
     */
    List<PluginInstancesInfo> selectAllRunningPluginInstanceInfos();

    /**
     * 
     * @param status
     * @param instanceName
     * @return
     */
    List<PluginInstances> selectAllByContainerStatusAndInstanceName(@Param("status") String status,
            @Param("instanceName") String instanceName);
}