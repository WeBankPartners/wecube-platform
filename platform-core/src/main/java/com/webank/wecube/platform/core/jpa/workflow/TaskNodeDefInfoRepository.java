package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;

public interface TaskNodeDefInfoRepository extends JpaRepository<TaskNodeDefInfoEntity, String> {

    @Query("select t from TaskNodeDefInfoEntity t "
            + " where t.procDefId = :procDefId and t.nodeId = :nodeId and t.status = :status")
    TaskNodeDefInfoEntity findOneWithProcessIdAndNodeIdAndStatus(@Param("procDefId") String procDefId,
            @Param("nodeId") String nodeId, @Param("status") String status);
    
    List<TaskNodeDefInfoEntity> findAllByProcDefId(String procDefId);
}
