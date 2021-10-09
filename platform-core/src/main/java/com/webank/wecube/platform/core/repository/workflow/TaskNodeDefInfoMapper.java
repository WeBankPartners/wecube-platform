package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;

@Repository
public interface TaskNodeDefInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(TaskNodeDefInfoEntity record);

    int insertSelective(TaskNodeDefInfoEntity record);

    TaskNodeDefInfoEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(TaskNodeDefInfoEntity record);

    int updateByPrimaryKey(TaskNodeDefInfoEntity record);

    /**
     * 
     * @param procDefId
     * @param nodeId
     * @param status
     * @return
     */
    TaskNodeDefInfoEntity selectOneWithProcessIdAndNodeIdAndStatus(@Param("procDefId") String procDefId,
            @Param("nodeId") String nodeId, @Param("status") String status);

    /**
     * 
     * @param procDefId
     * @return
     */
    List<TaskNodeDefInfoEntity> selectAllByProcDefId(@Param("procDefId") String procDefId);

    /**
     * 
     * @param procDefId
     * @param status
     * @return
     */
    List<TaskNodeDefInfoEntity> selectAllByProcDefIdAndStatus(@Param("procDefId") String procDefId,
            @Param("status") String status);
    
    
    /**
     * 
     * @param procDefId
     * @param status
     * @return
     */
    List<TaskNodeDefInfoEntity> selectAllByProcDefIdAndNodeId(@Param("procDefId") String procDefId,
            @Param("nodeId") String nodeId);
    
    /**
     * 
     * @return
     */
    List<String> selectAllBoundServices();
    
    /**
     * 
     * @param serviceId
     * @param status
     * @return
     */
    List<TaskNodeDefInfoEntity> selectAllByServiceAndStatus(@Param("serviceId") String serviceId,
            @Param("status") String status);
}
