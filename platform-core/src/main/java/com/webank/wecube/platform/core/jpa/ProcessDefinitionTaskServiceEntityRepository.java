package com.webank.wecube.platform.core.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.domain.workflow.ProcessDefinitionTaskServiceEntity;

public interface ProcessDefinitionTaskServiceEntityRepository
		extends JpaRepository<ProcessDefinitionTaskServiceEntity, String> {
	
	List<ProcessDefinitionTaskServiceEntity> findTaskServicesByProcDefKeyAndVersion(String procDefKey, Integer version);
	
	List<ProcessDefinitionTaskServiceEntity> findTaskServicesByProcDefKeyAndVersionAndTaskNodeId(String procDefKey, Integer version, String taskNodeId);
	
	@Query("select t from ProcessDefinitionTaskServiceEntity t  where t.coreProcDefId = :coreProcDefId")
	List<ProcessDefinitionTaskServiceEntity> findAllByProcDefId(@Param("coreProcDefId") String coreProcDefId);
}
