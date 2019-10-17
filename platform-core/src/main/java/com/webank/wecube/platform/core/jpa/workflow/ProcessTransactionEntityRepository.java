package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webank.wecube.platform.core.entity.workflow.ProcessTransactionEntity;

public interface ProcessTransactionEntityRepository extends JpaRepository<ProcessTransactionEntity, Integer> {
	
	@Query("select DISTINCT t from ProcessTransactionEntity t LEFT JOIN FETCH t.tasks where t.operator = :operator")
	List<ProcessTransactionEntity> findAllByOperator(String operator);
} 
