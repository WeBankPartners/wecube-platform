package com.webank.wecube.platform.core.repository.workflow;

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

    List<TaskNodeExecParamEntity> findAllByRequestIdAndParamNameAndParamType(@Param("requestId") String requestId,
            @Param("paramName") String paramName, @Param("paramType") String paramType);

    List<TaskNodeExecParamEntity> findAllByRequestIdAndParamType(@Param("requestId") String requestId,
            @Param("paramType") String paramType);

    List<TaskNodeExecParamEntity> findOneByRequestIdAndParamTypeAndParamNameAndValue(@Param("requestId") String requestId,
            @Param("paramType") String paramType, @Param("paramName") String paramName,
            @Param("paramDataValue") String paramDataValue);

}
