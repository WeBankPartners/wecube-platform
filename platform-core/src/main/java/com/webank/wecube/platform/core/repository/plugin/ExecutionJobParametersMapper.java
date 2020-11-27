package com.webank.wecube.platform.core.repository.plugin;

import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.ExecutionJobParameters;

@Repository
public interface ExecutionJobParametersMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(ExecutionJobParameters record);

    int insertSelective(ExecutionJobParameters record);

    ExecutionJobParameters selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ExecutionJobParameters record);

    int updateByPrimaryKey(ExecutionJobParameters record);
}