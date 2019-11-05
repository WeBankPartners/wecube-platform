package com.webank.wecube.platform.core.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;

public interface ProcInstInfoRepository extends JpaRepository<ProcInstInfoEntity, Integer> {
    
    ProcInstInfoEntity findOneByProcInstKernelId(String procInstKernelId);

}
