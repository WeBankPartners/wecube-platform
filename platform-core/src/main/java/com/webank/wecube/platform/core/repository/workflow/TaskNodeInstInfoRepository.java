package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;

public interface TaskNodeInstInfoRepository extends JpaRepository<TaskNodeInstInfoEntity, Integer> {
    
    List<TaskNodeInstInfoEntity> findAllByProcInstId(Integer procInstId);
    
    TaskNodeInstInfoEntity findOneByProcInstIdAndNodeId(Integer procInstId, String nodeId);
    
}
