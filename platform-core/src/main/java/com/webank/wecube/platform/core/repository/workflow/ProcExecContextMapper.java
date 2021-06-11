package com.webank.wecube.platform.core.repository.workflow;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.workflow.ProcExecContextEntity;

@Repository
public interface ProcExecContextMapper {

    int deleteByPrimaryKey(String id);

    int insert(ProcExecContextEntity record);

    ProcExecContextEntity selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ProcExecContextEntity record);

    List<ProcExecContextEntity> selectAllContextByCtxType(String procDefId, Integer procInstId, String ctxType);

}
