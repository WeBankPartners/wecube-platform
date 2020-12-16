package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
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

    TaskNodeDefInfoEntity selectOneWithProcessIdAndNodeIdAndStatus(@Param("procDefId") String procDefId,
            @Param("nodeId") String nodeId, @Param("status") String status);

    List<TaskNodeDefInfoEntity> selectAllByProcDefId(@Param("procDefId") String procDefId);

    List<TaskNodeDefInfoEntity> selectAllByProcDefIdAndStatus(@Param("procDefId") String procDefId,
            @Param("status") String status);
}
