package com.webank.wecube.platform.core.repository.plugin;

import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.CoreObjectListVar;

@Repository
public interface CoreObjectListVarMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreObjectListVar record);

    int insertSelective(CoreObjectListVar record);

    CoreObjectListVar selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreObjectListVar record);

    int updateByPrimaryKey(CoreObjectListVar record);
}