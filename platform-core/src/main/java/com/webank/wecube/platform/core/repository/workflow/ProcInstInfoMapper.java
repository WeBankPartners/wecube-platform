package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;

@Repository
public interface ProcInstInfoMapper{
    int deleteByPrimaryKey(Integer id);
    int insert(ProcInstInfoEntity record);
    int insertSelective(ProcInstInfoEntity record);
    ProcInstInfoEntity selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(ProcInstInfoEntity record);
    int updateByPrimaryKey(ProcInstInfoEntity record);
    
    @Query("select t from ProcInstInfoEntity t " + " where t.procInstKernelId = :procInstKernelId ")
    ProcInstInfoEntity findOneByProcInstKernelId(@Param("procInstKernelId") String procInstKernelId);

    @Query(value = "select * from core_ru_proc_inst_info where proc_def_id in (:procDefIds) ", nativeQuery = true)
    List<ProcInstInfoEntity> findByProcDefIdIn(@Param("procDefIds") List<String> procDefIds);
    
    @Query("select t from ProcInstInfoEntity t " + " where t.procDefId = :procDefId ")
    List<ProcInstInfoEntity> findAllByProcDefId(@Param("procDefId") String procDefId);
    
}
