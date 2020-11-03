package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreRuProcExecBindingTmp;

public interface CoreRuProcExecBindingTmpMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CoreRuProcExecBindingTmp record);

    int insertSelective(CoreRuProcExecBindingTmp record);

    CoreRuProcExecBindingTmp selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CoreRuProcExecBindingTmp record);

    int updateByPrimaryKey(CoreRuProcExecBindingTmp record);
}