package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webank.wecube.platform.core.entity.workflow.ProcessTaskEntity;

public interface ProcessTaskEntityRepository extends JpaRepository<ProcessTaskEntity, Integer> {
	
    @Query("select t from ProcessTaskEntity t  where t.transaction.id = :transactionId")
    List<ProcessTaskEntity> findAllByTransaction(Integer transactionId);
    
    List<ProcessTaskEntity> findTaskByProcessInstanceKey(String processInstanceKey);
}
