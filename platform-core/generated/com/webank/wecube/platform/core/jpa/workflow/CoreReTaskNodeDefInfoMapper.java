package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreReTaskNodeDefInfo;

public interface CoreReTaskNodeDefInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreReTaskNodeDefInfo record);

    int insertSelective(CoreReTaskNodeDefInfo record);

    CoreReTaskNodeDefInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreReTaskNodeDefInfo record);

    int updateByPrimaryKey(CoreReTaskNodeDefInfo record);
}