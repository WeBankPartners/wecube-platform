package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginMysqlInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginMysqlInstanceRepositoryTest extends DatabaseBasedTest {

    @Autowired
    private PluginPackageRepository pluginPackageRepository;
    @Autowired
    private PluginMysqlInstanceRepository pluginMysqlInstanceRepository;

    @Test
    public void findByStatusAndPluginPackage_nameTest() {
        String schemaName = "service_mgmt";
        String packageName = "service-mgmt";
        long currentTimeMillis = System.currentTimeMillis();
        PluginPackage pkg = new PluginPackage(null, packageName, "v0.1", REGISTERED,
                new Timestamp(currentTimeMillis - 500000), false);
        pluginPackageRepository.save(pkg);
        
        PluginMysqlInstance mysqlInstance = new PluginMysqlInstance(schemaName, null, "service_mgmt", "123", "active",
                pkg);
        pluginMysqlInstanceRepository.save(mysqlInstance);

        List<PluginMysqlInstance> mysqlInstances = pluginMysqlInstanceRepository
                .findByStatusAndPluginPackage_name("active", packageName);
        assertThat(mysqlInstances.size()).isEqualTo(1);
        assertThat(mysqlInstances.get(0).getSchemaName()).isEqualTo(schemaName);
    }
    
    @Test
    public void findByStatusAndPluginPackage_NameTest() {
        String schemaName = "service_mgmt";
        String packageName = "service-mgmt";
        long currentTimeMillis = System.currentTimeMillis();
        PluginPackage pkg = new PluginPackage(null, packageName, "v0.1", REGISTERED,
                new Timestamp(currentTimeMillis - 500000), false);
        pluginPackageRepository.save(pkg);
        
        PluginMysqlInstance mysqlInstance = new PluginMysqlInstance(schemaName, null, "service_mgmt", "123", "active",
                pkg);
        pluginMysqlInstanceRepository.save(mysqlInstance);

        List<PluginMysqlInstance> mysqlInstances = pluginMysqlInstanceRepository
                .findByStatusAndPluginPackage_Name("active", packageName);
        assertThat(mysqlInstances.size()).isEqualTo(1);
        assertThat(mysqlInstances.get(0).getSchemaName()).isEqualTo(schemaName);
    }
}