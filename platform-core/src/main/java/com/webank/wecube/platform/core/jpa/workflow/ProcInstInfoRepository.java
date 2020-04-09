package com.webank.wecube.platform.core.jpa.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;

public interface ProcInstInfoRepository extends JpaRepository<ProcInstInfoEntity, Integer> {
    
    ProcInstInfoEntity findOneByProcInstKernelId(String procInstKernelId);

    @Query(value = "select * from core_ru_proc_inst_info where proc_def_id in (:procDefIds) ", nativeQuery = true)
    List<ProcInstInfoEntity> findByProcDefIdIn(@Param("procDefIds") List<String> procDefIds);
    
    @Query("select t from ProcInstInfoEntity t " + " where t.procDefId = :procDefId ")
    List<ProcInstInfoEntity> findAllByProcDefId(@Param("procDefId") String procDefId);
}
