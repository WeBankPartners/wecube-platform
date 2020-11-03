package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreRuProcExecBinding;

public interface CoreRuProcExecBindingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CoreRuProcExecBinding record);

    int insertSelective(CoreRuProcExecBinding record);

    CoreRuProcExecBinding selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CoreRuProcExecBinding record);

    int updateByPrimaryKey(CoreRuProcExecBinding record);
}