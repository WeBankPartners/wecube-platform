package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageEntities;

@Repository
public interface PluginPackageEntitiesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageEntities record);

    int insertSelective(PluginPackageEntities record);

    PluginPackageEntities selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageEntities record);

    int updateByPrimaryKey(PluginPackageEntities record);

    /**
     * 
     * @param packageName
     * @param name
     * @param dataModelVersion
     * @return
     */
    List<PluginPackageEntities> selectAllByPackageNameAndEntityNameAndDataModelVersion(@Param("packageName")String packageName, @Param("name")String name,
            @Param("version")int version);
    
    /**
     * 
     * @param dataModelId
     * @return
     */
    List<PluginPackageEntities> selectAllByDataModel(@Param("dataModelId")String dataModelId);
}