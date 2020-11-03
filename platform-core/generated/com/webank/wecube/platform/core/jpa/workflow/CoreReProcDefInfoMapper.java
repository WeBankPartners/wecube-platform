package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreReProcDefInfo;

public interface CoreReProcDefInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreReProcDefInfo record);

    int insertSelective(CoreReProcDefInfo record);

    CoreReProcDefInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreReProcDefInfo record);

    int updateByPrimaryKeyWithBLOBs(CoreReProcDefInfo record);

    int updateByPrimaryKey(CoreReProcDefInfo record);
}