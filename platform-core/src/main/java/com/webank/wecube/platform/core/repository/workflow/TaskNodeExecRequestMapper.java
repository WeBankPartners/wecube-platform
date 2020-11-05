package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;

@Repository
public interface TaskNodeExecRequestMapper {
    int deleteByPrimaryKey(String reqId);

    int insert(TaskNodeExecRequestEntity record);

    int insertSelective(TaskNodeExecRequestEntity record);

    TaskNodeExecRequestEntity selectByPrimaryKey(String reqId);

    int updateByPrimaryKeySelective(TaskNodeExecRequestEntity record);

    int updateByPrimaryKey(TaskNodeExecRequestEntity record);

    TaskNodeExecRequestEntity findOneByRequestId(@Param("requestId") String requestId);

    List<TaskNodeExecRequestEntity> findCurrentEntityByNodeInstId(@Param("nodeInstId") Integer nodeInstId);
}
