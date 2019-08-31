package com.webank.wecube.core.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.core.domain.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.core.domain.workflow.entity.TraceStatus;

public interface ServiceNodeStatusRepository extends JpaRepository<ServiceNodeStatusEntity, String> {
    ServiceNodeStatusEntity findOneByProcInstanceBizKeyAndNodeId(String procInstanceBizKey, String nodeId);
    ServiceNodeStatusEntity findOneByProcInstanceIdAndNodeId(String procInstanceId, String nodeId);
    ServiceNodeStatusEntity findOneByProcInstanceBizKeyAndNodeIdAndStatus(String procInstanceBizKey, String nodeId, TraceStatus status);
}
