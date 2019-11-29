package com.webank.wecube.platform.core.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webank.wecube.platform.core.domain.plugin.PluginMysqlInstance;

public interface PluginMysqlInstanceRepository extends JpaRepository<PluginMysqlInstance, Integer> {
    List<PluginMysqlInstance> findByPluginPackageIdAndStatus(Integer pluginPackageId, String status);

    PluginMysqlInstance findByPluginPackageId(int packageId);

    List<PluginMysqlInstance> findBySchemaNameAndStatus(String schemaName, String status);

}
