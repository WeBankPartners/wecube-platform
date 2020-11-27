package com.webank.wecube.platform.core.entity.workflow;

import org.springframework.stereotype.Repository;

@Repository
public interface CoreOperationEventMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CoreOperationEvent record);

    int insertSelective(CoreOperationEvent record);

    CoreOperationEvent selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CoreOperationEvent record);

    int updateByPrimaryKey(CoreOperationEvent record);
}