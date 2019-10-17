package com.webank.wecube.platform.core.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.ProcessDefInfoEntity;

public interface ProcessDefInfoRepository extends JpaRepository<ProcessDefInfoEntity, String> {
//    ProcessDefInfoEntity findByProcDefKeyAndVersion(String procDefKey, Integer version);

    @Query("select p from ProcessDefInfoEntity p where p.procDefId = :processId and p.status = :status")
    ProcessDefInfoEntity findOneByProcessIdAndStatus(@Param("processId") String processId,
            @Param("status") String status);
}
