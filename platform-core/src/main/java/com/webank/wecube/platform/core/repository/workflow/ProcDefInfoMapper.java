package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcDefAuthInfoQueryEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;

@Repository
public interface ProcDefInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(ProcDefInfoEntity record);

    int insertSelective(ProcDefInfoEntity record);

    ProcDefInfoEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ProcDefInfoEntity record);

    int updateByPrimaryKey(ProcDefInfoEntity record);

    List<ProcDefInfoEntity> selectAllDeployedOrDraftProcDefs();

    List<ProcDefInfoEntity> selectAllDeployedProcDefsByProcDefName(@Param("procDefName") String procDefName);

    List<ProcDefInfoEntity> selectAllDeployedProcDefsByProcDefKey(@Param("procDefKey") String procDefKey,
            @Param("status") String status);
    
    List<ProcDefAuthInfoQueryEntity> selectAllAuthorizedProcDefs(@Param("roleNames") Set<String> roleNames);

}
