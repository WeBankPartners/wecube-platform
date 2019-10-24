package com.webank.wecube.platform.core.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;

public interface TaskNodeExecParamRepository extends JpaRepository<TaskNodeExecParamEntity, Integer> {

}
