package com.webank.wecube.platform.core.repository.plugin;

import com.webank.wecube.platform.core.entity.plugin.SystemVariables;

public interface SystemVariablesMapper {
    int deleteByPrimaryKey(String id);

    int insert(SystemVariables record);

    int insertSelective(SystemVariables record);

    SystemVariables selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SystemVariables record);

    int updateByPrimaryKey(SystemVariables record);
}