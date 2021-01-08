package com.webank.wecube.platform.core.repository.plugin;

import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectVar;

@Repository
public interface CoreObjectVarMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreObjectVar record);

    int insertSelective(CoreObjectVar record);

    CoreObjectVar selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreObjectVar record);

    int updateByPrimaryKey(CoreObjectVar record);
}