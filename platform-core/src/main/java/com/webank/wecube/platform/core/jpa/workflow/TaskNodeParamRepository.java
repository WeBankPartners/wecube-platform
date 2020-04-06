package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;

public interface TaskNodeParamRepository extends JpaRepository<TaskNodeParamEntity, String> {

    List<TaskNodeParamEntity> findAllByProcDefId(String procDefId);

    @Query("select t from TaskNodeParamEntity t " + " where t.procDefId = :procDefId and t.status = :status ")
    List<TaskNodeParamEntity> findAllByProcDefIdAndStatus(@Param("procDefId") String procDefId,
            @Param("status") String status);

    @Query("select t from TaskNodeParamEntity t "
            + " where t.procDefId = :procDefId and t.taskNodeDefId = :taskNodeDefId ")
    List<TaskNodeParamEntity> findAllByProcDefIdAndTaskNodeDefId(@Param("procDefId") String procDefId,
            @Param("taskNodeDefId") String taskNodeDefId);
    
    
    @Query("select t from TaskNodeParamEntity t "
            + " where t.procDefId = :procDefId and t.taskNodeDefId = :taskNodeDefId and t.status = 'draft' ")
    List<TaskNodeParamEntity> findAllDraftByProcDefIdAndTaskNodeDefId(@Param("procDefId") String procDefId,
            @Param("taskNodeDefId") String taskNodeDefId);

    @Query("select t from TaskNodeParamEntity t "
            + " where t.taskNodeDefId = :taskNodeDefId and t.paramName = :paramName")
    TaskNodeParamEntity findOneByTaskNodeDefIdAndParamName(@Param("taskNodeDefId") String taskNodeDefId,
            @Param("paramName") String paramName);
}
