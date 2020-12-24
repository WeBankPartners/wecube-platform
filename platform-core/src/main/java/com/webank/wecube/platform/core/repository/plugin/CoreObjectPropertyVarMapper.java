package com.webank.wecube.platform.core.repository.plugin;

import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectPropertyVar;

@Repository
public interface CoreObjectPropertyVarMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreObjectPropertyVar record);

    int insertSelective(CoreObjectPropertyVar record);

    CoreObjectPropertyVar selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreObjectPropertyVar record);

    int updateByPrimaryKey(CoreObjectPropertyVar record);
}