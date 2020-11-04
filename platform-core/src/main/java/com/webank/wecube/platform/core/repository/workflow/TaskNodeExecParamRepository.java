package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;

public interface TaskNodeExecParamRepository extends JpaRepository<TaskNodeExecParamEntity, Integer> {

    @Query("select t from TaskNodeExecParamEntity t "
            + " where t.requestId = :requestId and t.paramName = :paramName and t.paramType = :paramType")
    List<TaskNodeExecParamEntity> findAllByRequestIdAndParamNameAndParamType(@Param("requestId") String requestId,
            @Param("paramName") String paramName, @Param("paramType") String paramType);

    @Query("select t from TaskNodeExecParamEntity t " + " where t.requestId = :requestId and t.paramType = :paramType ")
    List<TaskNodeExecParamEntity> findAllByRequestIdAndParamType(@Param("requestId") String requestId,
            @Param("paramType") String paramType);

    @Query("select t from TaskNodeExecParamEntity t "
            + " where t.requestId = :requestId and t.paramType = :paramType and t.paramName = :paramName")
    List<TaskNodeExecParamEntity> findAllByRequestIdAndParamTypeAndParamName(@Param("requestId") String requestId,
            @Param("paramType") String paramType, @Param("paramName") String paramName);

    @Query("select t from TaskNodeExecParamEntity t "
            + " where t.requestId = :requestId and t.paramType = :paramType and t.paramName = :paramName and t.paramDataValue = :paramDataValue")
    List<TaskNodeExecParamEntity> findOneByRequestIdAndParamTypeAndParamNameAndValue(@Param("requestId") String requestId,
            @Param("paramType") String paramType, @Param("paramName") String paramName,
            @Param("paramDataValue") String paramDataValue);

}
