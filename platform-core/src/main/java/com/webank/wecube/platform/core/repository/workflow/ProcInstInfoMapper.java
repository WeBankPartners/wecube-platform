package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

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
    
    ProcInstInfoEntity findOneByProcInstKernelId(@Param("procInstKernelId") String procInstKernelId);

    List<ProcInstInfoEntity> findByProcDefIdIn(@Param("procDefIds") List<String> procDefIds);
    
    List<ProcInstInfoEntity> findAllByProcDefId(@Param("procDefId") String procDefId);
    
}
