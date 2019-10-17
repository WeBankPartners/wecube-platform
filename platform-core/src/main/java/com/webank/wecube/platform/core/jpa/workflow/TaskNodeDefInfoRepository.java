package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;

public interface TaskNodeDefInfoRepository extends JpaRepository<TaskNodeDefInfoEntity, String> {

//    List<TaskNodeDefInfoEntity> findTaskServicesByProcDefKeyAndVersion(String procDefKey, Integer version);
//
//    List<TaskNodeDefInfoEntity> findTaskServicesByProcDefKeyAndVersionAndTaskNodeId(String procDefKey, Integer version,
//            String taskNodeId);

//    @Query("select t from ProcessDefinitionTaskServiceEntity t  where t.coreProcDefId = :coreProcDefId")
//    List<TaskNodeDefInfoEntity> findAllByProcDefId(@Param("coreProcDefId") String coreProcDefId);

    @Query("select t from TaskNodeDefInfoEntity t "
            + " where t.procDefId = :processId and t.taskNodeId = :nodeId and t.status = :status")
    TaskNodeDefInfoEntity findOneWithProcessIdAndNodeIdAndStatus(@Param("processId") String processId,
            @Param("nodeId") String nodeId, @Param("status") String status);
}
