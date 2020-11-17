package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.AuthLatestEnabledInterfaces;
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

    /**
     * 
     * @param pluginConfigStatus
     * @param pluginPackageStatuses
     * @return
     */
    List<AuthLatestEnabledInterfaces> selectAllAuthorizedLatestEnabledIntfs(
            @Param("pluginConfigStatus") String pluginConfigStatus,
            @Param("pluginPackageStatuses") List<String> pluginPackageStatuses,
            @Param("permissionType") String permissionType, @Param("roleNames") List<String> roleNames);

    /**
     * 
     * @param targetPackage
     * @param targetEntity
     * @param configStatus
     * @return
     */
    List<AuthLatestEnabledInterfaces> selectAllAuthEnabledIntfsByTargetInfo(
            @Param("targetPackage") String targetPackage, @Param("targetEntity") String targetEntity,
            @Param("pluginConfigStatus") String pluginConfigStatus, @Param("permissionType") String permissionType, @Param("roleNames") List<String> roleNames,
            @Param("pluginPackageStatuses") List<String> pluginPackageStatuses);
    
    
    /**
     * 
     * @param targetPackage
     * @param targetEntity
     * @param pluginConfigStatus
     * @param permissionType
     * @param roleNames
     * @param pluginPackageStatuses
     * @return
     */
    List<AuthLatestEnabledInterfaces> selectAllAuthEnabledIntfsByTargetInfoAndNullFilterRule(
            @Param("targetPackage") String targetPackage, @Param("targetEntity") String targetEntity,
            @Param("pluginConfigStatus") String pluginConfigStatus, @Param("permissionType") String permissionType, @Param("roleNames") List<String> roleNames,
            @Param("pluginPackageStatuses") List<String> pluginPackageStatuses);
    
    
    /**
     * 
     * @param targetPackage
     * @param targetEntity
     * @param pluginConfigStatus
     * @param permissionType
     * @param roleNames
     * @param pluginPackageStatuses
     * @param filterRule
     * @return
     */
    List<AuthLatestEnabledInterfaces> selectAllAuthEnabledIntfsByTargetInfoAndFilterRule(
            @Param("targetPackage") String targetPackage, @Param("targetEntity") String targetEntity,
            @Param("pluginConfigStatus") String pluginConfigStatus, @Param("permissionType") String permissionType, @Param("roleNames") List<String> roleNames,
            @Param("pluginPackageStatuses") List<String> pluginPackageStatuses, @Param("filterRule") String filterRule);
    
    /**
     * 
     * @param pluginConfigStatus
     * @param permissionType
     * @param roleNames
     * @param pluginPackageStatuses
     * @param filterRule
     * @return
     */
    List<AuthLatestEnabledInterfaces> selectAllAuthEnabledIntfsByNullTargetInfo(
            @Param("pluginConfigStatus") String pluginConfigStatus, @Param("permissionType") String permissionType, @Param("roleNames") List<String> roleNames,
            @Param("pluginPackageStatuses") List<String> pluginPackageStatuses);

}