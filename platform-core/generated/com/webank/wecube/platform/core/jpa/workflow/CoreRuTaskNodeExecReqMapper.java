package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreRuTaskNodeExecReq;

public interface CoreRuTaskNodeExecReqMapper {
    int deleteByPrimaryKey(String reqId);

    int insert(CoreRuTaskNodeExecReq record);

    int insertSelective(CoreRuTaskNodeExecReq record);

    CoreRuTaskNodeExecReq selectByPrimaryKey(String reqId);

    int updateByPrimaryKeySelective(CoreRuTaskNodeExecReq record);

    int updateByPrimaryKey(CoreRuTaskNodeExecReq record);
}