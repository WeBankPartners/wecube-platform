package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreRuProcInstInfo;

public interface CoreRuProcInstInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CoreRuProcInstInfo record);

    int insertSelective(CoreRuProcInstInfo record);

    CoreRuProcInstInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CoreRuProcInstInfo record);

    int updateByPrimaryKey(CoreRuProcInstInfo record);
}