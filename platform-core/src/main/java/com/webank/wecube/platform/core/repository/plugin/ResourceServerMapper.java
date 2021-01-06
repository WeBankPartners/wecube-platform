package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.ResourceServer;
import com.webank.wecube.platform.core.entity.plugin.ResourceServerExample;

@Repository
public interface ResourceServerMapper {
    int countByExample(ResourceServerExample example);

    int deleteByExample(ResourceServerExample example);

    int deleteByPrimaryKey(String id);

    int insert(ResourceServer record);

    int insertSelective(ResourceServer record);

    List<ResourceServer> selectByExample(ResourceServerExample example);

    ResourceServer selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ResourceServer record, @Param("example") ResourceServerExample example);

    int updateByExample(@Param("record") ResourceServer record, @Param("example") ResourceServerExample example);

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