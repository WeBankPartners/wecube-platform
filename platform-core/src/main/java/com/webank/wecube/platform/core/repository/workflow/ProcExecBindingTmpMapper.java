package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingTmpEntity;

@Repository
public interface ProcExecBindingTmpMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(ProcExecBindingTmpEntity record);

    int insertSelective(ProcExecBindingTmpEntity record);

    ProcExecBindingTmpEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProcExecBindingTmpEntity record);

    int updateByPrimaryKey(ProcExecBindingTmpEntity record);

    /**
     * 
     * @param nodeDefId
     * @param processSessionId
     * @return
     */
    List<ProcExecBindingTmpEntity> selectAllNodeBindingsByNodeAndSession(@Param("nodeDefId") String nodeDefId,
            @Param("processSessionId") String processSessionId);

    /**
     * 
     * @param processSessionId
     * @return
     */
    List<ProcExecBindingTmpEntity> selectAllNodeBindingsBySession(@Param("processSessionId") String processSessionId);

    /**
     * 
     * @param processSessionId
     * @return
     */
    List<ProcExecBindingTmpEntity> selectAllRootBindingsBySession(@Param("processSessionId") String processSessionId);
}
