package com.webank.wecube.core.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.core.domain.workflow.entity.ProcessInstanceStatusEntity;

public interface ProcessInstanceStatusRepository extends JpaRepository<ProcessInstanceStatusEntity, String> {
    ProcessInstanceStatusEntity findOneByprocInstanceId(String procInstanceId);
}
