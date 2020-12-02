package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.entity.plugin.ResourceItemExample;

@Repository
public interface ResourceItemMapper {
    int countByExample(ResourceItemExample example);

    int deleteByExample(ResourceItemExample example);

    int deleteByPrimaryKey(String id);

    int insert(ResourceItem record);

    int insertSelective(ResourceItem record);

    List<ResourceItem> selectByExample(ResourceItemExample example);

    ResourceItem selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ResourceItem record, @Param("example") ResourceItemExample example);

    int updateByExample(@Param("record") ResourceItem record, @Param("example") ResourceItemExample example);

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