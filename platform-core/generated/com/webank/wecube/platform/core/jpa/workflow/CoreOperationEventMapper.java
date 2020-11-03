package com.webank.wecube.platform.core.jpa.workflow;

import com.webank.wecube.platform.core.entity.workflow.CoreOperationEvent;

public interface CoreOperationEventMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CoreOperationEvent record);

    int insertSelective(CoreOperationEvent record);

    CoreOperationEvent selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CoreOperationEvent record);

    int updateByPrimaryKey(CoreOperationEvent record);
}