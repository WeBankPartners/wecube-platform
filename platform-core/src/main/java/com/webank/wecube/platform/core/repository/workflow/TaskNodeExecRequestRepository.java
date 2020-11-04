package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;

public interface TaskNodeExecRequestRepository extends JpaRepository<TaskNodeExecRequestEntity, String> {

    TaskNodeExecRequestEntity findOneByRequestId(String requestId);

    @Query("select t from TaskNodeExecRequestEntity t " + " where t.nodeInstId = :nodeInstId and t.current = true")
    List<TaskNodeExecRequestEntity> findCurrentEntityByNodeInstId(@Param("nodeInstId") Integer nodeInstId);
}
