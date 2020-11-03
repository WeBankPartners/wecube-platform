package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreReTaskNodeParam;

public interface CoreReTaskNodeParamMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreReTaskNodeParam record);

    int insertSelective(CoreReTaskNodeParam record);

    CoreReTaskNodeParam selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreReTaskNodeParam record);

    int updateByPrimaryKey(CoreReTaskNodeParam record);
}