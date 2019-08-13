package com.webank.wecube.core.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.core.domain.workflow.TaskNodeExecLogEntity;

public interface TaskNodeExecLogEntityRepository extends JpaRepository<TaskNodeExecLogEntity, Integer> {

    TaskNodeExecLogEntity findEntityByInstanceBusinessKeyAndTaskNodeId(String instanceBusinessKey, String taskNodeId);
    
    List<TaskNodeExecLogEntity> findEntitiesByInstanceBusinessKey(String instanceBusinessKey);
}
