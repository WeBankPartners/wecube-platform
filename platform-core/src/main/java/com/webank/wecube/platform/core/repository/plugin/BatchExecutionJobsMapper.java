package com.webank.wecube.platform.core.repository.plugin;

import org.springframework.stereotype.Repository;

import com.webank.wecube.platform.core.entity.plugin.BatchExecutionJobs;

@Repository
public interface BatchExecutionJobsMapper {

    int deleteByPrimaryKey(String id);

    int insert(BatchExecutionJobs record);

    int insertSelective(BatchExecutionJobs record);

    BatchExecutionJobs selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BatchExecutionJobs record);

    int updateByPrimaryKey(BatchExecutionJobs record);
}