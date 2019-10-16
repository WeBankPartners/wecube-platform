package com.webank.wecube.platform.core.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.entity.workflow.ProcessDefInfoEntity;

public interface ProcessDefInfoRepository extends JpaRepository<ProcessDefInfoEntity, String> {
	ProcessDefInfoEntity findByProcDefKeyAndVersion(String procDefKey, Integer version);
}
