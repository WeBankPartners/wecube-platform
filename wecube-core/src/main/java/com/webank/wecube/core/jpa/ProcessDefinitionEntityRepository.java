package com.webank.wecube.core.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.core.domain.workflow.ProcessDefinitionEntity;

public interface ProcessDefinitionEntityRepository extends JpaRepository<ProcessDefinitionEntity, String> {
	ProcessDefinitionEntity findByProcDefKeyAndVersion(String procDefKey, Integer version);
}
