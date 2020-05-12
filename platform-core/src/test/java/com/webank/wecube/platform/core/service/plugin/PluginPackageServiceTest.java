package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.parser.PluginPackageValidator;
import com.webank.wecube.platform.core.service.PluginInstanceService;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PluginPackageServiceTest extends DatabaseBasedTest {
    @Autowired
    private PluginPackageRepository packageRepository;

    @Autowired
    private PluginInstanceRepository instanceRepository;

    @Autowired
    private PluginConfigRepository configRepository;

    @Autowired
    private PluginPackageService pluginPackageService;

    @Autowired
    private PluginInstanceService instanceService;

    @Test
    public void givenThereIsStillRunningInstanceForPluginPackageWhenDecommissionThenShouldThrowException() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PluginPackage pluginPackage = new PluginPackage(null, "qcloud", "v1.0", PluginPackage.Status.REGISTERED, now, false);
        PluginPackage savedPluginPackage = packageRepository.save(pluginPackage);

        PluginInstance instance = new PluginInstance(null, savedPluginPackage, "qcloud", "127.0.0.1", 20000, PluginInstance.CONTAINER_STATUS_RUNNING);

        instanceRepository.save(instance);

        try {
            pluginPackageService.decommissionPluginPackage(savedPluginPackage.getId());
        } catch (Exception e) {
            assertThat(e instanceof WecubeCoreException).isTrue();
            assertThat(e.getMessage()).isEqualTo("Decommission plugin package [qcloud__v1.0] failure. There are still 1 plugin instance running");
        }
    }

    @Test
    public void givenThereIsNoRunningInstanceForPluginPackageWhenDecommissionThenShouldSucceed() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PluginPackage pluginPackage = new PluginPackage(null, "qcloud", "v1.0", PluginPackage.Status.REGISTERED, now, false);
        PluginPackage savedPluginPackage = packageRepository.save(pluginPackage);

        List<PluginInstance> runningInstances = instanceService.getAvailableInstancesByPackageId(savedPluginPackage.getId());
        assertThat(runningInstances==null || runningInstances.size()==0).isTrue();

        PluginConfig pluginConfig = new PluginConfig(null, savedPluginPackage, "vpc", "wecmdb__2__network_zone", "network_zone", PluginConfig.Status.ENABLED, null);
        PluginConfig savedPluginConfig = configRepository.save(pluginConfig);

        assertThat(savedPluginConfig.getStatus()).isEqualTo(PluginConfig.Status.ENABLED);

        try {
            pluginPackageService.decommissionPluginPackage(savedPluginPackage.getId());
            assertThat(true).isTrue();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        Optional<PluginConfig> configByIdOptional = configRepository.findById(savedPluginConfig.getId());
        assertThat(configByIdOptional.isPresent()).isTrue();
        assertThat(configByIdOptional.get().getStatus()).isEqualTo(PluginConfig.Status.DISABLED);
    }

    @Test
    public void validatePackageVersionTest() {
        String version1 = "v1.1.1";
        String version2 = "v1.1.1.1";
        String version3 = "v0.1.1";
        String version4 = "v0.1.1.0";
        String version5 = "1.1.1";
        String version6 = "version1.1.1";
        String version7 = "ls";
        String version8 = "uname";
        String version9 = "pwd";

        String version10 = "v&uname";
        String version11 = "v1.0.0&uanme";
        String version12 = "v1.0.0 & ls";
        String version13 = "v1.0.0;ls";
        String version14 = "v1.0.0&' ls";
        String version15 = "v1.0.0&\" ls";
        String version16 = "v1.0.0'&ls";
        String version17 = "v1.0.0\"&ls";
        String version18 = "v1.0.0'& ls";
        String version19 = "v1.0.0'; ls";
        String version20 = "v1.0.0;'ls";

        validateVersionSuccessful(version1);
        validateVersionSuccessful(version2);
        validateVersionSuccessful(version3);
        validateVersionSuccessful(version4);

        validateVersionFailed(version6);
        validateVersionFailed(version7);
        validateVersionFailed(version8);
        validateVersionFailed(version9);
        validateVersionFailed(version10);
        validateVersionFailed(version11);
        validateVersionFailed(version12);
        validateVersionFailed(version13);
        validateVersionFailed(version14);
        validateVersionFailed(version15);
        validateVersionFailed(version16);
        validateVersionFailed(version17);
        validateVersionFailed(version18);
        validateVersionFailed(version19);
        validateVersionFailed(version20);

    }

    void validateVersionFailed(String ver) {
        try {
            PluginPackageValidator.validatePackageVersion(ver);
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo(String.format("Invalid plugin package version [%s].", ver));
        }
    }

    void validateVersionSuccessful(String ver) {
        try {
            PluginPackageValidator.validatePackageVersion(ver);
            assertThat(true).isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo(String.format("Invalid plugin package version [%s].", ver));
        }
    }

}