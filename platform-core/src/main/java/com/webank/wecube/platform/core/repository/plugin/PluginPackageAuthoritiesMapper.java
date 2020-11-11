package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginPackageAuthorities;

@Repository
public interface PluginPackageAuthoritiesMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginPackageAuthorities record);

    int insertSelective(PluginPackageAuthorities record);

    PluginPackageAuthorities selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginPackageAuthorities record);

    int updateByPrimaryKey(PluginPackageAuthorities record);
    
    List<PluginPackageAuthorities> selectAllByPackage(@Param("pluginPackageId")String pluginPackageId);
}