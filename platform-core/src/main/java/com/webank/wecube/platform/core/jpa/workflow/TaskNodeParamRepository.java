package com.webank.wecube.platform.core.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;

public interface TaskNodeParamRepository extends JpaRepository<TaskNodeParamEntity, String> {

}
