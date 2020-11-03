package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreRuTaskNodeInstInfo;

public interface CoreRuTaskNodeInstInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CoreRuTaskNodeInstInfo record);

    int insertSelective(CoreRuTaskNodeInstInfo record);

    CoreRuTaskNodeInstInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CoreRuTaskNodeInstInfo record);

    int updateByPrimaryKey(CoreRuTaskNodeInstInfo record);
}