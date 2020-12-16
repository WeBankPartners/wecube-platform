package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.GraphNodeEntity;

@Repository
public interface GraphNodeMapper{
    
    int deleteByPrimaryKey(Integer id);

    int insert(GraphNodeEntity record);

    int insertSelective(GraphNodeEntity record);

    GraphNodeEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(GraphNodeEntity record);


    int updateByPrimaryKey(GraphNodeEntity record);

    /**
     * 
     * @param processSessionId
     * @return
     */
    List<GraphNodeEntity> selectAllByProcessSessionId(@Param("processSessionId") String processSessionId);

    /**
     * 
     * @param procInstId
     * @return
     */
    List<GraphNodeEntity> selectAllByProcInstId(@Param("procInstId") Integer procInstId);
}
