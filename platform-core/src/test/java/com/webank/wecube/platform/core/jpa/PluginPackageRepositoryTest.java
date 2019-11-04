package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginPackageRepositoryTest  extends DatabaseBasedTest {

    @Autowired
    private PluginPackageRepository pluginPackageRepository;

    @Test
    public void givenMultiplePluginPackageWithDifferentStatusWhenFindIdsByStatusThenShouldReturnCorrectIds() {
        long currentTimeMillis = System.currentTimeMillis();
        PluginPackage serviceManagementV1PluginPackage = new PluginPackage(1, "service-management", "v0.1", DECOMMISSIONED, new Timestamp(currentTimeMillis - 500000), false);
        PluginPackage serviceManagementV2PluginPackage = new PluginPackage(2, "service-management", "v0.2", STOPPED, new Timestamp(currentTimeMillis - 400000), false);
        PluginPackage serviceManagementV3PluginPackage = new PluginPackage(3, "service-management", "v0.3", RUNNING, new Timestamp(currentTimeMillis - 300000), false);
        PluginPackage serviceManagementV4PluginPackage = new PluginPackage(4, "service-management", "v0.4", UNREGISTERED, new Timestamp(currentTimeMillis - 200000), false);
        PluginPackage cmdbPluginPackage = new PluginPackage(5, "cmdb", "v1.0", REGISTERED, new Timestamp(currentTimeMillis - 100000), false);
        PluginPackage cmdbPluginPackage2 = new PluginPackage(6, "cmdb", "v2.0", UNREGISTERED, new Timestamp(currentTimeMillis), false);

        pluginPackageRepository.saveAll(Arrays.asList(serviceManagementV1PluginPackage, serviceManagementV2PluginPackage, serviceManagementV3PluginPackage, serviceManagementV4PluginPackage, cmdbPluginPackage, cmdbPluginPackage2));

        Optional<Set<PluginPackage>> pluginPackagesOptional = pluginPackageRepository.findLatestPluginPackagesByStatusGroupByPackageName(REGISTERED, RUNNING, STOPPED);
        assertThat(pluginPackagesOptional.isPresent()).isTrue();

        Set<PluginPackage> pluginPackages = pluginPackagesOptional.get();
        assertThat(pluginPackages).hasSize(2);
        assertThat(pluginPackages.contains(serviceManagementV3PluginPackage)).isTrue();
        assertThat(pluginPackages.contains(cmdbPluginPackage)).isTrue();
    }
}