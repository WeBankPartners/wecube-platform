package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;

public interface TaskNodeParamRepository extends JpaRepository<TaskNodeParamEntity, String> {

    List<TaskNodeParamEntity> findAllByProcDefId(String procDefId);

    List<TaskNodeParamEntity> findAllByProcDefIdAndTaskNodeDefId(String procDefId, String taskNodeDefId);

    @Query("select t from TaskNodeParamEntity t "
            + " where t.taskNodeDefId = :taskNodeDefId and t.paramName = :paramName")
    TaskNodeParamEntity findOneByTaskNodeDefIdAndParamName(@Param("taskNodeDefId") String taskNodeDefId,
            @Param("paramName") String paramName);
}
