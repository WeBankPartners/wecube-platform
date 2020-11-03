package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreRuTaskNodeExecParam;

public interface CoreRuTaskNodeExecParamMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CoreRuTaskNodeExecParam record);

    int insertSelective(CoreRuTaskNodeExecParam record);

    CoreRuTaskNodeExecParam selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CoreRuTaskNodeExecParam record);

    int updateByPrimaryKey(CoreRuTaskNodeExecParam record);
}