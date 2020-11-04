package com.webank.wecube.platform.core.jpa.workflow;

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

//    @Query("select t from ProcDefInfoEntity t "
//            + " where t.active = true and t.deleted = false and t.procDefKey = :procDefKey and t.status = :status")
    List<ProcDefInfoEntity> findAllDeployedProcDefsByProcDefKey(@Param("procDefKey") String procDefKey,
            @Param("status") String status);

}
