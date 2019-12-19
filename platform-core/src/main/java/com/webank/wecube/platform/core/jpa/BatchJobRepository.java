package com.webank.wecube.platform.core.jpa;
import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.BatchJob;

public interface BatchJobRepository extends  CrudRepository<BatchJob, Integer> {

}