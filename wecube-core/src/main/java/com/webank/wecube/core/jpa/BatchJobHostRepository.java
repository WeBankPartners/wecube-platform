package com.webank.wecube.core.jpa;
import com.webank.wecube.core.domain.BatchJobHost;
import com.webank.wecube.core.domain.plugin.PluginConfig;
import com.webank.wecube.core.domain.plugin.PluginConfigFilteringRule;
import com.webank.wecube.core.domain.plugin.PluginConfigInterface;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BatchJobHostRepository extends  CrudRepository<BatchJobHost, Integer> {
    @Query("SELECT host FROM BatchJobHost host WHERE host.batchJobId = :batchId")
    List<BatchJobHost> findBatchJobByBatchId(Integer batchId);

    @Query("SELECT host FROM BatchJobHost host WHERE host.batchJobId = :batchId and host.hostIp = :hostIp")
    List<BatchJobHost> findBatchJobByBatchIdAndHostIP(Integer batchId, String hostIp);
}

