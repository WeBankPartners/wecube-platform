package com.webank.wecube.platform.core.service.plugin;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.jpa.PluginConfigRepository;
import com.webank.wecube.platform.core.jpa.PluginInstanceRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
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

        PluginInstance instance = new PluginInstance(null, savedPluginPackage, "qcloud", "10.0.2.12", 20000, PluginInstance.CONTAINER_STATUS_RUNNING);

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
}