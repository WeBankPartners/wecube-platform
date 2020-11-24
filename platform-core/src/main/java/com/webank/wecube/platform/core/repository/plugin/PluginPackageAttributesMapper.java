package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageAttributes;

@Repository
public interface PluginPackageAttributesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageAttributes record);

    int insertSelective(PluginPackageAttributes record);

    PluginPackageAttributes selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageAttributes record);

    int updateByPrimaryKey(PluginPackageAttributes record);

    /**
     * 
     * @param entityId
     * @return
     */
    List<PluginPackageAttributes> selectAllByEntity(@Param("entityId") String entityId);

    /**
     * 
     * @param attributeId
     * @return
     */
    List<PluginPackageAttributes> selectAllReferences(@Param("attributeId") String attributeId);

    /**
     * 
     * @param dataModelId
     * @return
     */
    List<PluginPackageAttributes> selectAllRefAttributesToRefreshByDataModel(@Param("dataModelId") String dataModelId);

    /**
     * 
     * @param packageName
     * @param entityName
     * @param attrName
     * @return
     */
    PluginPackageAttributes selectLatestAttributeByPackageAndEntityAndAttr(@Param("packageName") String packageName,
            @Param("entityName") String entityName, @Param("attrName") String attrName);

}