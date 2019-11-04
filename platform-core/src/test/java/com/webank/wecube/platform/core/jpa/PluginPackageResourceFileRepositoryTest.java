package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageResourceFile;
import org.apache.commons.compress.utils.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginPackageResourceFileRepositoryTest  extends DatabaseBasedTest {

    public static final String SERVICE_MANAGEMENT = "service-management";
    public static final String CMDB = "cmdb";
    public static final String UI_ZIP = "ui.zip";
    @Autowired
    private PluginPackageResourceFileRepository pluginPackageResourceFileRepository;
    @Autowired
    private PluginPackageRepository pluginPackageRepository;

    @Test
    public void givenMultiplePackageWithResourceFilesWhenQueryThenReturnCorrectFilenameList() {
        long currentTimeMillis = System.currentTimeMillis();
        PluginPackage serviceManagementV1PluginPackage = new PluginPackage(1, SERVICE_MANAGEMENT, "v0.1", DECOMMISSIONED, new Timestamp(currentTimeMillis - 500000), false);
        PluginPackage serviceManagementV2PluginPackage = new PluginPackage(2, SERVICE_MANAGEMENT, "v0.2", STOPPED, new Timestamp(currentTimeMillis - 400000), false);
        PluginPackage serviceManagementV3PluginPackage = new PluginPackage(3, SERVICE_MANAGEMENT, "v0.3", RUNNING, new Timestamp(currentTimeMillis - 300000), false);
        PluginPackage serviceManagementV4PluginPackage = new PluginPackage(4, SERVICE_MANAGEMENT, "v0.4", UNREGISTERED, new Timestamp(currentTimeMillis - 200000), false);
        PluginPackage cmdbPluginPackage = new PluginPackage(5, CMDB, "v1.0", REGISTERED, new Timestamp(currentTimeMillis - 100000), false);
        PluginPackage cmdbPluginPackage2 = new PluginPackage(6, CMDB, "v2.0", UNREGISTERED, new Timestamp(currentTimeMillis), false);

        PluginPackageResourceFile sm1PluginPackageResourceFile = new PluginPackageResourceFile(1, serviceManagementV1PluginPackage, SERVICE_MANAGEMENT, "v0.1", UI_ZIP, SERVICE_MANAGEMENT + "/" + "dist/sm-v1.js");
        PluginPackageResourceFile sm2PluginPackageResourceFile = new PluginPackageResourceFile(2, serviceManagementV2PluginPackage, SERVICE_MANAGEMENT, "v0.2", UI_ZIP, SERVICE_MANAGEMENT + "/" + "dist/sm-v2.js");
        PluginPackageResourceFile sm3PluginPackageResourceFile = new PluginPackageResourceFile(3, serviceManagementV3PluginPackage, SERVICE_MANAGEMENT, "v0.3", UI_ZIP, SERVICE_MANAGEMENT + "/" + "dist/sm-v3.js");
        PluginPackageResourceFile sm4PluginPackageResourceFile = new PluginPackageResourceFile(4, serviceManagementV4PluginPackage, SERVICE_MANAGEMENT, "v0.4", UI_ZIP, SERVICE_MANAGEMENT + "/" + "dist/sm-v4.js");
        PluginPackageResourceFile cmdb1PluginPackageResourceFile = new PluginPackageResourceFile(5, cmdbPluginPackage, CMDB, "v1.0", UI_ZIP, CMDB + "/" + "dist/cmdb-v1.js");
        PluginPackageResourceFile cmdb2PluginPackageResourceFile = new PluginPackageResourceFile(6, cmdbPluginPackage2, CMDB, "v2.0", UI_ZIP, CMDB + "/" + "dist/cmdb-v2.js");

        serviceManagementV1PluginPackage.setPluginPackageResourceFiles(Sets.newHashSet(sm1PluginPackageResourceFile));
        serviceManagementV2PluginPackage.setPluginPackageResourceFiles(Sets.newHashSet(sm2PluginPackageResourceFile));
        serviceManagementV3PluginPackage.setPluginPackageResourceFiles(Sets.newHashSet(sm3PluginPackageResourceFile));
        serviceManagementV4PluginPackage.setPluginPackageResourceFiles(Sets.newHashSet(sm4PluginPackageResourceFile));
        cmdbPluginPackage.setPluginPackageResourceFiles(Sets.newHashSet(cmdb1PluginPackageResourceFile));
        cmdbPluginPackage2.setPluginPackageResourceFiles(Sets.newHashSet(cmdb2PluginPackageResourceFile));

        pluginPackageRepository.saveAll(Arrays.asList(serviceManagementV1PluginPackage, serviceManagementV2PluginPackage, serviceManagementV3PluginPackage, serviceManagementV4PluginPackage, cmdbPluginPackage, cmdbPluginPackage2));

        pluginPackageResourceFileRepository.saveAll(Arrays.asList(sm1PluginPackageResourceFile, sm2PluginPackageResourceFile, sm3PluginPackageResourceFile, sm4PluginPackageResourceFile, cmdb1PluginPackageResourceFile, cmdb2PluginPackageResourceFile));

        Optional<Set<PluginPackage>> pluginPackagesOptional = pluginPackageRepository.findLatestPluginPackagesByStatusGroupByPackageName(REGISTERED, RUNNING, STOPPED);
        assertThat(pluginPackagesOptional.isPresent()).isTrue();

        Set<PluginPackage> pluginPackages = pluginPackagesOptional.get();

        assertThat(pluginPackages).hasSize(2);
        assertThat(pluginPackages.contains(serviceManagementV3PluginPackage)).isTrue();
        assertThat(pluginPackages.contains(cmdbPluginPackage)).isTrue();
        Optional<List<PluginPackageResourceFile>> pluginPackageResourceFilesOptional = pluginPackageResourceFileRepository.findPluginPackageResourceFileByPluginPackageIds(pluginPackages.stream().map(p -> p.getId()).collect(Collectors.toSet()).toArray(new Integer[pluginPackages.size()]));

        assertThat(pluginPackageResourceFilesOptional.isPresent()).isTrue();
        List<PluginPackageResourceFile> pluginPackageResourceFiles = pluginPackageResourceFilesOptional.get();
        assertThat(pluginPackageResourceFiles).hasSize(2);
        assertThat(pluginPackageResourceFiles.contains(sm3PluginPackageResourceFile)).isTrue();
        assertThat(pluginPackageResourceFiles.contains(cmdb1PluginPackageResourceFile)).isTrue();

    }
}