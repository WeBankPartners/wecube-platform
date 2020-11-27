package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.OperationEventEntity;

@Repository
public interface OperationEventMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OperationEventEntity record);

    int insertSelective(OperationEventEntity record);

    OperationEventEntity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OperationEventEntity record);

    int updateByPrimaryKey(OperationEventEntity record);

    List<OperationEventEntity> selectAllByEventSeqNo(@Param("eventSeqNo") String eventSeqNo);

    List<OperationEventEntity> selectAllByStatus(@Param("status") String status);

    List<OperationEventEntity> findAllByProcInstKey(@Param("procInstKey") String procInstKey);
}