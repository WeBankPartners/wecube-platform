package com.webank.wecube.core.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.core.domain.workflow.ProcessDefinitionTaskServiceEntity;

public interface ProcessDefinitionTaskServiceEntityRepository
		extends JpaRepository<ProcessDefinitionTaskServiceEntity, String> {
	
	List<ProcessDefinitionTaskServiceEntity> findTaskServicesByProcDefKeyAndVersion(String procDefKey, Integer version);
	
	List<ProcessDefinitionTaskServiceEntity> findTaskServicesByProcDefKeyAndVersionAndTaskNodeId(String procDefKey, Integer version, String taskNodeId);
}
