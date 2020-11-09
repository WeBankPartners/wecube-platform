package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.ResourceServer;

public interface ResourceServerMapper {
    int deleteByPrimaryKey(String id);

    int insert(ResourceServer record);

    int insertSelective(ResourceServer record);

    ResourceServer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ResourceServer record);

    int updateByPrimaryKey(ResourceServer record);
}