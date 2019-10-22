package com.webank.wecube.platform.workflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.workflow.entity.ProcessInstanceStatusEntity;

public interface ProcessInstanceStatusRepository extends JpaRepository<ProcessInstanceStatusEntity, String> {
    ProcessInstanceStatusEntity findOneByprocInstanceId(String procInstanceId);
}
