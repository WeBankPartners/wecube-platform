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

    /**
     * 
     * @param eventSeqNo
     * @return
     */
    List<OperationEventEntity> selectAllByEventSeqNo(@Param("eventSeqNo") String eventSeqNo);

    /**
     * 
     * @param status
     * @return
     */
    List<OperationEventEntity> selectAllByStatus(@Param("status") String status);

    /**
     * 
     * @param procInstKey
     * @return
     */
    List<OperationEventEntity> selectAllByProcInstKey(@Param("procInstKey") String procInstKey);
    
    /**
     * 
     * @param record
     * @param expectRev
     * @return
     */
    int updateByPrimaryKeySelectiveCas(@Param("record") OperationEventEntity record, @Param("expectRev") int expectRev);
}