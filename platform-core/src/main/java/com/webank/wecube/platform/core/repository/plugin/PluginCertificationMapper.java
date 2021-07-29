package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.PluginCertification;

@Repository
public interface PluginCertificationMapper {
    int deleteByPrimaryKey(String id);

    int insert(PluginCertification record);

    int insertSelective(PluginCertification record);

    PluginCertification selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PluginCertification record);

    int updateByPrimaryKey(PluginCertification record);
    
    /**
     * 
     * @return
     */
    List<PluginCertification> selectAllPluginCertifications();
    
    /**
     * 
     * @param plugin
     * @return
     */
    PluginCertification selectPluginCertificationByPlugin(@Param("plugin")String plugin);
}
