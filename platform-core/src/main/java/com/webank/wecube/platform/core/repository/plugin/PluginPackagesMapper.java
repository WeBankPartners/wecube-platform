package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackages;
import com.webank.wecube.platform.core.entity.plugin.SimplePluginPackageInfo;

@Repository
public interface PluginPackagesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackages record);

    int insertSelective(PluginPackages record);

    PluginPackages selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackages record);

    int updateByPrimaryKey(PluginPackages record);

    /**
     * 
     * @param name
     * @param version
     * @return
     */
    int countByNameAndVersion(@Param("name") String name, @Param("version") String version,@Param("edition") String edition);

    /**
     * 
     * @return
     */
    List<PluginPackages> selectAll();

    /**
     * 
     * @return
     */
    List<PluginPackages> selectAllDistinctPackages();

    /**
     * 
     * @param name
     * @return
     */
    List<PluginPackages> selectAllByName(@Param("name") String name);

    /**
     * 
     * @param name
     * @param statuses
     * @return
     */
    List<PluginPackages> selectAllByNameAndStatuses(@Param("name") String name,
            @Param("statuses") List<String> statuses);

    /**
     * 
     * @param name
     * @param version
     * @return
     */
    List<PluginPackages> selectAllByNameAndVersion(@Param("name") String name, @Param("version") String version);

    /**
     * 
     * @param statuses
     * @return
     */
    List<PluginPackages> selectAllLatestUploadedPackages(@Param("statuses") List<String> statuses);
    
    
    /**
     * 
     * @return
     */
    List<SimplePluginPackageInfo> selectAllActivePluginPackageEntities();

}