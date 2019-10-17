package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecLogEntity;

public interface TaskNodeExecLogEntityRepository extends JpaRepository<TaskNodeExecLogEntity, Integer> {

    TaskNodeExecLogEntity findEntityByInstanceBusinessKeyAndTaskNodeId(String instanceBusinessKey, String taskNodeId);
    
    List<TaskNodeExecLogEntity> findEntitiesByInstanceBusinessKey(String instanceBusinessKey);
}
