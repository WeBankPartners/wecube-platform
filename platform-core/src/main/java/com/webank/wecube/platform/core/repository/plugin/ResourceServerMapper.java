package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.ResourceServer;

@Repository
public interface ResourceServerMapper {
    int deleteByPrimaryKey(String id);

    int insert(ResourceServer record);

    int insertSelective(ResourceServer record);

    ResourceServer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ResourceServer record);

    int updateByPrimaryKey(ResourceServer record);
    
    /**
     * 
     * @param host
     * @param type
     * @return
     */
    List<ResourceServer> selectAllByHostAndType(@Param("host")String host, @Param("type")String type);
    
    /**
     * 
     * @param type
     * @return
     */
    List<ResourceServer> selectAllByType(@Param("type")String type);
}