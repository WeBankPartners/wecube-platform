package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.SystemVariables;
import com.webank.wecube.platform.core.entity.plugin.SystemVariablesExample;

@Repository
public interface SystemVariablesMapper {
    int countByExample(SystemVariablesExample example);

    int deleteByExample(SystemVariablesExample example);

    int deleteByPrimaryKey(String id);

    int insert(SystemVariables record);

    int insertSelective(SystemVariables record);

    List<SystemVariables> selectByExample(SystemVariablesExample example);

    SystemVariables selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") SystemVariables record, @Param("example") SystemVariablesExample example);

    int updateByExample(@Param("record") SystemVariables record, @Param("example") SystemVariablesExample example);

    int updateByPrimaryKeySelective(SystemVariables record);

    int updateByPrimaryKey(SystemVariables record);

    /**
     * 
     * @param name
     * @param scope
     * @param status
     * @return
     */
    List<SystemVariables> selectAllByNameAndScopeAndStatus(@Param("name") String name, @Param("scope") String scope,
            @Param("status") String status);

    /**
     * 
     * @param name
     * @param scope
     * @param source
     * @return
     */
    List<SystemVariables> selectAllByNameAndScopeAndSource(@Param("name") String name, @Param("scope") String scope,
            @Param("source") String source);
    
    
//    /**
//     * 
//     * @param packageName
//     * @return
//     */
//    List<SystemVariables> selectAllByPluginPackages(@Param("pluginPackageIds") List<String> pluginPackageIds);
     
//    /**
//     * 
//     * @param pluginPackageId
//     * @return
//     */
//    List<SystemVariables> selectAllByPluginPackage(@Param("pluginPackageId") String pluginPackageId);
    
    
    /**
     * 
     * @param sourceList
     * @return
     */
    List<SystemVariables> selectAllBySourceList(@Param("sourceList") List<String> sourceList);
    
    /**
     * 
     * @param source
     * @return
     */
    List<SystemVariables> selectAllBySource(@Param("source") String source);
    
    
    /**
     * 
     * @return
     */
    List<String> selectAllSystemVariableScopes();
}