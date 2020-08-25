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

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed1() {
        PluginPackageValidator.validatePackageVersion("version1.1.1");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed2() {
        PluginPackageValidator.validatePackageVersion("ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed3() {
        PluginPackageValidator.validatePackageVersion("v&uname");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed4() {
        PluginPackageValidator.validatePackageVersion("uname");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed5() {
        PluginPackageValidator.validatePackageVersion("v1.0.0&uanme");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed6() {
        PluginPackageValidator.validatePackageVersion("v1.0.0 & ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed7() {
        PluginPackageValidator.validatePackageVersion("v1.0.0;ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed8() {
        PluginPackageValidator.validatePackageVersion("v1.0.0&' ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed9() {
        PluginPackageValidator.validatePackageVersion("v1.0.0&\" ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed10() {
        PluginPackageValidator.validatePackageVersion("v1.0.0'&ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed11() {
        PluginPackageValidator.validatePackageVersion("v1.0.0\"&ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed12() {
        PluginPackageValidator.validatePackageVersion("v1.0.0'& ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed13() {
        PluginPackageValidator.validatePackageVersion("v1.0.0'; ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed14() {
        PluginPackageValidator.validatePackageVersion("v1.0.0;'ls");
    }

    @Test(expected = WecubeCoreException.class)
    public void validatePackageVersionTestShouldFailed15() {
        PluginPackageValidator.validatePackageVersion("v100.1.1.2");
    }

    @Test
    public void validateVersionShouldSuccess1() {
        PluginPackageValidator.validatePackageVersion("v1.1.1");
        assertThat(true).isTrue();
    }

    @Test
    public void validateVersionShouldSuccess2() {
        PluginPackageValidator.validatePackageVersion("v1.1.1.1");
        assertThat(true).isTrue();
    }

    @Test
    public void validateVersionShouldSuccess3() {
        PluginPackageValidator.validatePackageVersion("v0.1.1");
        assertThat(true).isTrue();
    }

    @Test
    public void validateVersionShouldSuccess4() {
        PluginPackageValidator.validatePackageVersion("v0.1.1.0");
        assertThat(true).isTrue();
    }

    @Test
    public void validateVersionShouldSuccess5() {
        PluginPackageValidator.validatePackageVersion("v0.1");
        assertThat(true).isTrue();
    }

    @Test
    public void validateVersionShouldSuccess6() {
        PluginPackageValidator.validatePackageVersion("v1.4.2.18");
        assertThat(true).isTrue();
    }

}