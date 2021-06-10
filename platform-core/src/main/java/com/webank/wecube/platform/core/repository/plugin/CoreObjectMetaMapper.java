package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectMeta;

@Repository
public interface CoreObjectMetaMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreObjectMeta record);

    int insertSelective(CoreObjectMeta record);

    CoreObjectMeta selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreObjectMeta record);

    int updateByPrimaryKey(CoreObjectMeta record);

    /**
     * 
     * @param packageName
     * @param name
     * @param configId
     * @return
     */
    CoreObjectMeta selectOneByPackageNameAndObjectNameAndConfig(@Param("packageName") String packageName,
            @Param("name") String name,@Param("configId") String configId);
    
    /**
     * 
     * @param configId
     * @return
     */
    List<CoreObjectMeta> selectAllByConfig(@Param("configId") String configId );

}