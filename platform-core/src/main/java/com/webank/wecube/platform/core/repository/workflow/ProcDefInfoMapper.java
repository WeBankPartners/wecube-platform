package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;

@Repository
public interface ProcDefInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(ProcDefInfoEntity record);

    int insertSelective(ProcDefInfoEntity record);

    ProcDefInfoEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ProcDefInfoEntity record);

    int updateByPrimaryKey(ProcDefInfoEntity record);

    List<ProcDefInfoEntity> findAllDeployedOrDraftProcDefs();

    List<ProcDefInfoEntity> findAllDeployedProcDefsByProcDefName(@Param("procDefName") String procDefName);

    List<ProcDefInfoEntity> findAllDeployedProcDefsByProcDefKey(@Param("procDefKey") String procDefKey,
            @Param("status") String status);

}
