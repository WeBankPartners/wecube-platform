package com.webank.wecube.platform.core.repository.plugin;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyMeta;

@Repository
public interface CoreObjectPropertyMetaMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreObjectPropertyMeta record);

    int insertSelective(CoreObjectPropertyMeta record);

    CoreObjectPropertyMeta selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreObjectPropertyMeta record);

    int updateByPrimaryKey(CoreObjectPropertyMeta record);
    
    /**
     * 
     * @param objectMetaId
     * @return
     */
    List<CoreObjectPropertyMeta> selectAllByObjectMeta(@Param("objectMetaId")String objectMetaId);
    
    /**
     * 
     * @param objectMetaId
     * @return
     */
    int deleteByObjectMeta(@Param("objectMetaId")String objectMetaId );
}