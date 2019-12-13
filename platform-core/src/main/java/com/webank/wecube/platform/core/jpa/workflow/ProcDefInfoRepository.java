package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import org.springframework.data.repository.query.Param;

public interface ProcDefInfoRepository extends JpaRepository<ProcDefInfoEntity, String> {

    @Query("select t from ProcDefInfoEntity t "
            + " where t.active = true and (t.status = 'deployed' or t.status = 'draft')")
    List<ProcDefInfoEntity> findAllDeployedOrDraftProcDefs();

    @Query("select t from ProcDefInfoEntity t "
            + " where t.active = true and t.id=:procId and(t.status = 'deployed' or t.status = 'draft')")
    List<ProcDefInfoEntity> findAllDeployedOrDraftProcDefsById(@Param("procId") String procId);

    @Query("select t from ProcDefInfoEntity t "
            + " where t.active = true and t.status = 'deployed' ")
    List<ProcDefInfoEntity> findAllDeployedProcDefs();

//    @Transactional
//    @Modifying
//    @Query("delete from ProcessDefInfoEntity t where t.id = :id")
//    void deleteByEntityId(@Param("id") String id);
}
