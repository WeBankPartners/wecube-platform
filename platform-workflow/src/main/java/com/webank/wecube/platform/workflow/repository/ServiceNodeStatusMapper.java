package com.webank.wecube.platform.workflow.repository;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.workflow.entity.ServiceNodeStatusEntity;
import com.webank.wecube.platform.workflow.model.TraceStatus;

@Repository
public interface ServiceNodeStatusMapper {
    /**
     * 
     * @param id
     * @return
     */
    int deleteByPrimaryKey(String id);
    /**
     * 
     * @param record
     * @return
     */
    int insert(ServiceNodeStatusEntity record);
    /**
     * 
     * @param record
     * @return
     */
    int insertSelective(ServiceNodeStatusEntity record);
    /**
     * 
     * @param id
     * @return
     */
    ServiceNodeStatusEntity selectByPrimaryKey(String id);
    /**
     * 
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(ServiceNodeStatusEntity record);
    /**
     * 
     * @param record
     * @return
     */
    int updateByPrimaryKey(ServiceNodeStatusEntity record);
    
    //############
    /**
     * 
     * @param procInstKey
     * @param nodeId
     * @return
     */
    ServiceNodeStatusEntity findOneByProcInstanceBizKeyAndNodeId(@Param("procInstKey")String procInstKey, @Param("nodeId")String nodeId);
    /**
     * 
     * @param procInstId
     * @param nodeId
     * @return
     */
    ServiceNodeStatusEntity findOneByProcInstanceIdAndNodeId(@Param("procInstId")String procInstId, @Param("nodeId")String nodeId);
    
    /**
     * 
     * @param procInstKey
     * @param nodeId
     * @param status
     * @return
     */
    ServiceNodeStatusEntity findOneByProcInstanceBizKeyAndNodeIdAndStatus(@Param("procInstKey")String procInstKey, @Param("nodeId")String nodeId, @Param("status")TraceStatus status);
}
