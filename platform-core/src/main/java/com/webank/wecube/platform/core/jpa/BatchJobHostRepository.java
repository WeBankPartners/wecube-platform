package com.webank.wecube.platform.core.jpa;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.webank.wecube.platform.core.domain.BatchJobHost;

public interface BatchJobHostRepository extends  CrudRepository<BatchJobHost, Integer> {
    @Query("SELECT host FROM BatchJobHost host WHERE host.batchJobId = :batchId")
    List<BatchJobHost> findBatchJobByBatchId(Integer batchId);

    @Query("SELECT host FROM BatchJobHost host WHERE host.batchJobId = :batchId and host.hostIp = :hostIp")
    List<BatchJobHost> findBatchJobByBatchIdAndHostIP(Integer batchId, String hostIp);
}

