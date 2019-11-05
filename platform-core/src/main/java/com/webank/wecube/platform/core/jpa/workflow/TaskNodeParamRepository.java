package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;

public interface TaskNodeParamRepository extends JpaRepository<TaskNodeParamEntity, String> {

    List<TaskNodeParamEntity> findAllByProcDefId(String procDefId);
    List<TaskNodeParamEntity> findAllByProcDefIdAndTaskNodeDefId(String procDefId, String taskNodeDefId);
}
