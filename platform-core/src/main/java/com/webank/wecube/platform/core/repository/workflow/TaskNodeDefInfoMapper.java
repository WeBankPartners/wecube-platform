package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;

@Repository
public interface TaskNodeDefInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(TaskNodeDefInfoEntity record);

    int insertSelective(TaskNodeDefInfoEntity record);

    TaskNodeDefInfoEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(TaskNodeDefInfoEntity record);

    int updateByPrimaryKey(TaskNodeDefInfoEntity record);

    @Query("select t from TaskNodeDefInfoEntity t "
            + " where t.procDefId = :procDefId and t.nodeId = :nodeId and t.status = :status")
    TaskNodeDefInfoEntity findOneWithProcessIdAndNodeIdAndStatus(@Param("procDefId") String procDefId,
            @Param("nodeId") String nodeId, @Param("status") String status);

    @Query("select t from TaskNodeDefInfoEntity t " + " where t.procDefId = :procDefId")
    List<TaskNodeDefInfoEntity> findAllByProcDefId(@Param("procDefId") String procDefId);

    @Query("select t from TaskNodeDefInfoEntity t " + " where t.procDefId = :procDefId and t.status = :status ")
    List<TaskNodeDefInfoEntity> findAllByProcDefIdAndStatus(@Param("procDefId") String procDefId,
            @Param("status") String status);
}
