package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.ResourceItem;

@Repository
public interface ResourceItemMapper {
    int deleteByPrimaryKey(String id);

    int insert(ResourceItem record);

    int insertSelective(ResourceItem record);

    ResourceItem selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ResourceItem record);

    int updateByPrimaryKey(ResourceItem record);

    /**
     * 
     * @param resourceServerId
     * @param type
     * @return
     */
    List<ResourceItem> selectAllByResourceServerAndType(@Param("resourceServerId") String resourceServerId,
            @Param("type") String type);
    
    
    /**
     * 
     * @param name
     * @param type
     * @return
     */
    List<ResourceItem> selectAllByNameAndType(@Param("name") String name,
            @Param("type") String type);
}