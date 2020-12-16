package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;

@Repository
public interface ProcExecBindingMapper{
    
    int deleteByPrimaryKey(Integer id);

    int insert(ProcExecBindingEntity record);

    int insertSelective(ProcExecBindingEntity record);

    ProcExecBindingEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProcExecBindingEntity record);

    int updateByPrimaryKey(ProcExecBindingEntity record);

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    List<ProcExecBindingEntity> selectAllTaskNodeBindings(@Param("procInstId") Integer procInstId,
            @Param("nodeInstId") Integer nodeInstId);

    /**
     * 
     * @param procInstId
     * @return
     */
    ProcExecBindingEntity selectProcInstBindings(@Param("procInstId") Integer procInstId);
    
    /**
     * 
     * @param procInstId
     * @return
     */
    List<ProcExecBindingEntity> selectAllTaskNodeBindingsByProcInstId(@Param("procInstId") Integer procInstId);
}
