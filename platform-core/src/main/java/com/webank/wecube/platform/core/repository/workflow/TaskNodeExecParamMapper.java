package com.webank.wecube.platform.core.repository.workflow;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;

@Repository
public interface TaskNodeExecParamMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(TaskNodeExecParamEntity record);

    int insertSelective(TaskNodeExecParamEntity record);

    TaskNodeExecParamEntity selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TaskNodeExecParamEntity record);

    int updateByPrimaryKey(TaskNodeExecParamEntity record);

    /**
     * 
     * @param requestId
     * @param paramName
     * @param paramType
     * @return
     */
    List<TaskNodeExecParamEntity> selectAllByRequestIdAndParamNameAndParamType(@Param("requestId") String requestId,
            @Param("paramName") String paramName, @Param("paramType") String paramType);

    /**
     * 
     * @param requestId
     * @param paramType
     * @return
     */
    List<TaskNodeExecParamEntity> selectAllByRequestIdAndParamType(@Param("requestId") String requestId,
            @Param("paramType") String paramType);

    /**
     * 
     * @param requestId
     * @param paramType
     * @param paramName
     * @param paramDataValue
     * @return
     */
    List<TaskNodeExecParamEntity> selectOneByRequestIdAndParamTypeAndParamNameAndValue(
            @Param("requestId") String requestId, @Param("paramType") String paramType,
            @Param("paramName") String paramName, @Param("paramDataValue") String paramDataValue);

    /**
     * 
     * @param nodeDefId
     * @param entityDataId
     * @return
     */
    int countSuccessTasknodeStatistics(@Param("nodeDefId") String nodeDefId, @Param("entityDataId") String entityDataId,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 
     * @param nodeDefId
     * @param entityDataId
     * @return
     */
    int countFailedTasknodeStatistics(@Param("nodeDefId") String nodeDefId, @Param("entityDataId") String entityDataId,
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
