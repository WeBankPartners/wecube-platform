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
    List<TaskNodeParamEntity> selectAllByProcDefId(@Param("procDefId")String procDefId);

    /**
     * 
     * @param procDefId
     * @param status
     * @return
     */
    List<TaskNodeParamEntity> selectAllByProcDefIdAndStatus(@Param("procDefId") String procDefId,
            @Param("status") String status);

    /**
     * 
     * @param procDefId
     * @param taskNodeDefId
     * @return
     */
    List<TaskNodeParamEntity> selectAllByProcDefIdAndTaskNodeDefId(@Param("procDefId") String procDefId,
            @Param("taskNodeDefId") String taskNodeDefId);
    
    
    /**
     * 
     * @param procDefId
     * @param taskNodeDefId
     * @return
     */
    List<TaskNodeParamEntity> selectAllDraftByProcDefIdAndTaskNodeDefId(@Param("procDefId") String procDefId,
            @Param("taskNodeDefId") String taskNodeDefId);

    /**
     * 
     * @param taskNodeDefId
     * @param paramName
     * @return
     */
    TaskNodeParamEntity selectOneByTaskNodeDefIdAndParamName(@Param("taskNodeDefId") String taskNodeDefId,
            @Param("paramName") String paramName);
}
