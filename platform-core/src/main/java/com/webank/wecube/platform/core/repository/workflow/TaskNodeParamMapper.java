package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;

@Repository
public interface TaskNodeParamMapper {
    
    int deleteByPrimaryKey(String id);

    int insert(TaskNodeParamEntity record);

    int insertSelective(TaskNodeParamEntity record);

    TaskNodeParamEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(TaskNodeParamEntity record);

    int updateByPrimaryKey(TaskNodeParamEntity record);

    /**
     * 
     * @param procDefId
     * @return
     */
    List<TaskNodeParamEntity> findAllByProcDefId(@Param("procDefId")String procDefId);

    /**
     * 
     * @param procDefId
     * @param status
     * @return
     */
    List<TaskNodeParamEntity> findAllByProcDefIdAndStatus(@Param("procDefId") String procDefId,
            @Param("status") String status);

    /**
     * 
     * @param procDefId
     * @param taskNodeDefId
     * @return
     */
    List<TaskNodeParamEntity> findAllByProcDefIdAndTaskNodeDefId(@Param("procDefId") String procDefId,
            @Param("taskNodeDefId") String taskNodeDefId);
    
    
    /**
     * 
     * @param procDefId
     * @param taskNodeDefId
     * @return
     */
    List<TaskNodeParamEntity> findAllDraftByProcDefIdAndTaskNodeDefId(@Param("procDefId") String procDefId,
            @Param("taskNodeDefId") String taskNodeDefId);

    /**
     * 
     * @param taskNodeDefId
     * @param paramName
     * @return
     */
    TaskNodeParamEntity findOneByTaskNodeDefIdAndParamName(@Param("taskNodeDefId") String taskNodeDefId,
            @Param("paramName") String paramName);
}
