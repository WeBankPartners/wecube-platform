package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;

@Repository
public interface TaskNodeInstInfoMapper{
    
    int deleteByPrimaryKey(Integer id);

    int insert(TaskNodeInstInfoEntity record);

    int insertSelective(TaskNodeInstInfoEntity record);

    TaskNodeInstInfoEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TaskNodeInstInfoEntity record);

    int updateByPrimaryKey(TaskNodeInstInfoEntity record);
    
    /**
     * 
     * @param procInstId
     * @return
     */
    List<TaskNodeInstInfoEntity> selectAllByProcInstId(@Param("procInstId")Integer procInstId);
    
    /**
     * 
     * @param procInstId
     * @param nodeId
     * @return
     */
    TaskNodeInstInfoEntity selectOneByProcInstIdAndNodeId(@Param("procInstId")Integer procInstId, @Param("nodeId")String nodeId);
    
}
