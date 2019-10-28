package com.webank.wecube.platform.core.domain.plugin;

import com.webank.wecube.platform.core.domain.JsonResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import static com.webank.wecube.platform.core.domain.plugin.PluginConfig.Status.*;

public class PluginPackageTest {

    @Test
    public void givenNoPluginConfigWhenGetStatusThenReturnUNREGISTERED() {
        PluginPackage pluginPackage = new PluginPackage(1, "service-management", "v0.1", null, null, null, null, null, null, null, null);

        assertThat(pluginPackage.getStatus()).isEqualTo(UNREGISTERED.name());
        pluginPackage.setPluginConfigs(null);
        assertThat(pluginPackage.getStatus()).isEqualTo(UNREGISTERED.name());
        pluginPackage.setPluginConfigs(new LinkedHashSet<>());
        assertThat(pluginPackage.getStatus()).isEqualTo(UNREGISTERED.name());
    }

    @Test
    public void givenNoREGISTEREDPluginConfigWhenGetStatusThenReturnUNREGISTERED() {
        PluginPackage pluginPackage = new PluginPackage(1, "service-management", "v0.1", null, null, null, null, null, null, null, null);

        PluginConfig taskPluginConfig = new PluginConfig(1, pluginPackage, "task", 1, "task", UNREGISTERED, null);
        PluginConfig serviceRequestPluginConfig = new PluginConfig(2, pluginPackage, "service_request", 2, "service_request", UNREGISTERED, null);

        pluginPackage.setPluginConfigs(newLinkedHashSet(Arrays.asList(taskPluginConfig, serviceRequestPluginConfig)));

        assertThat(pluginPackage.getStatus()).isEqualTo(UNREGISTERED.name());

    }

    @Test
    public void givenOneOfPluginConfigsRegisteredWhenGetStatusThenReturnREGISTERED() {
        PluginPackage pluginPackage = new PluginPackage(1, "service-management", "v0.1", null, null, null, null, null, null, null, null);

        PluginConfig taskPluginConfig = new PluginConfig(1, pluginPackage, "task", 1, "task", UNREGISTERED, null);
        PluginConfig serviceRequestPluginConfig = new PluginConfig(2, pluginPackage, "service_request", 2, "service_request", UNREGISTERED, null);
        PluginConfig servicePipelinePluginConfig = new PluginConfig(3, pluginPackage, "service_pipeline", 3, "service_pipeline", REGISTERED, null);

        pluginPackage.setPluginConfigs(newLinkedHashSet(Arrays.asList(taskPluginConfig, serviceRequestPluginConfig, servicePipelinePluginConfig)));

        assertThat(pluginPackage.getStatus()).isEqualTo(REGISTERED.name());

    }

    @Test
    public void givenAllPluginConfigsRegisteredWhenGetStatusThenReturnREGISTERED() {
        PluginPackage pluginPackage = new PluginPackage(1, "service-management", "v0.1", null, null, null, null, null, null, null, null);

        PluginConfig taskPluginConfig = new PluginConfig(1, pluginPackage, "task", 1, "task", REGISTERED, null);
        PluginConfig serviceRequestPluginConfig = new PluginConfig(2, pluginPackage, "service_request", 2, "service_request", REGISTERED, null);
        PluginConfig servicePipelinePluginConfig = new PluginConfig(3, pluginPackage, "service_pipeline", 3, "service_pipeline", REGISTERED, null);

        pluginPackage.setPluginConfigs(newLinkedHashSet(Arrays.asList(taskPluginConfig, serviceRequestPluginConfig, servicePipelinePluginConfig)));

        assertThat(pluginPackage.getStatus()).isEqualTo(REGISTERED.name());

    }

    @Test
    public void givenAllPluginConfigsDecommissionedWhenGetStatusThenReturnUNREGISTERED() {
        PluginPackage pluginPackage = new PluginPackage(1, "service-management", "v0.1", null, null, null, null, null, null, null, null);

        PluginConfig taskPluginConfig = new PluginConfig(1, pluginPackage, "task", 1, "task", DECOMMISSIONED, null);
        PluginConfig serviceRequestPluginConfig = new PluginConfig(2, pluginPackage, "service_request", 2, "service_request", DECOMMISSIONED, null);
        PluginConfig servicePipelinePluginConfig = new PluginConfig(3, pluginPackage, "service_pipeline", 3, "service_pipeline", DECOMMISSIONED, null);

        pluginPackage.setPluginConfigs(newLinkedHashSet(Arrays.asList(taskPluginConfig, serviceRequestPluginConfig, servicePipelinePluginConfig)));

        assertThat(pluginPackage.getStatus()).isEqualTo(UNREGISTERED.name());

    }

}