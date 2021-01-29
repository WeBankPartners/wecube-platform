package com.webank.wecube.platform.core.repository.plugin;

import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.ExecutionJobs;

@Repository
public interface ExecutionJobsMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(ExecutionJobs record);

    int insertSelective(ExecutionJobs record);

    ExecutionJobs selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ExecutionJobs record);

    int updateByPrimaryKey(ExecutionJobs record);
}