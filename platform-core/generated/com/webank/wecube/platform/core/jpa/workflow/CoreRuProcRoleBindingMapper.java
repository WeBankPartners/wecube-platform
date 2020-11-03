package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreRuProcRoleBinding;

public interface CoreRuProcRoleBindingMapper {
    int deleteByPrimaryKey(String id);

    int insert(CoreRuProcRoleBinding record);

    int insertSelective(CoreRuProcRoleBinding record);

    CoreRuProcRoleBinding selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(CoreRuProcRoleBinding record);

    int updateByPrimaryKey(CoreRuProcRoleBinding record);
}