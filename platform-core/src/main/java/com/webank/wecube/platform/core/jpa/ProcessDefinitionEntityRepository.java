package com.webank.wecube.platform.core.jpa;

import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessDefinitionEntityRepository extends JpaRepository<ProcessDefinitionEntity, String> {
	ProcessDefinitionEntity findByProcDefKeyAndVersion(String procDefKey, Integer version);
}
