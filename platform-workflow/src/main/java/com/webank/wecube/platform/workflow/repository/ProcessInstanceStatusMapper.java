package com.webank.wecube.platform.workflow.repository;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.workflow.entity.ProcessInstanceStatusEntity;

@Repository
public interface ProcessInstanceStatusMapper{
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
    int insert(ProcessInstanceStatusEntity record);
    /**
     * 
     * @param record
     * @return
     */
    int insertSelective(ProcessInstanceStatusEntity record);
    /**
     * 
     * @param id
     * @return
     */
    ProcessInstanceStatusEntity selectByPrimaryKey(String id);
    /**
     * 
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(ProcessInstanceStatusEntity record);
    /**
     * 
     * @param record
     * @return
     */
    int updateByPrimaryKey(ProcessInstanceStatusEntity record);
    /**
     * 
     * @param procInstanceId
     * @return
     */
    ProcessInstanceStatusEntity findOneByprocInstanceId(@Param("procInstId")String procInstId);
}
