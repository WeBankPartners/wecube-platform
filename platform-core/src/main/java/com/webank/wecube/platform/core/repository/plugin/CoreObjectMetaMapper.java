package com.webank.wecube.platform.core.repository.plugin;

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
     * @param objectName
     * @return
     */
    CoreObjectMeta selectOneByPackageNameAndObjectName(@Param("packageName") String packageName,
            @Param("name") String name);

}