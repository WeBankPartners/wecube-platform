package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.ResourceItem;

public interface ResourceItemMapper {
    int deleteByPrimaryKey(String id);

    int insert(ResourceItem record);

    int insertSelective(ResourceItem record);

    ResourceItem selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ResourceItem record);

    int updateByPrimaryKey(ResourceItem record);
}