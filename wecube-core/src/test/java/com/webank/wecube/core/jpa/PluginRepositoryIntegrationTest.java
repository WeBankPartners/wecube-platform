package com.webank.wecube.core.jpa;

import com.webank.wecube.core.DatabaseBasedTest;
import com.webank.wecube.core.domain.plugin.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.core.domain.plugin.PluginConfig.Status.NOT_CONFIGURED;
import static com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter.TYPE_INPUT;
import static com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter.TYPE_OUTPUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;

public class PluginRepositoryIntegrationTest extends DatabaseBasedTest {

    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginConfigRepository pluginConfigRepository;
    @Autowired
    PluginInstanceRepository pluginInstanceRepository;

    @Test
    public void findAllSavedPluginPackages() {
        PluginPackage pluginPackage = mockPluginPackage("mockPluginPackage", "v1");
        pluginPackageRepository.save(pluginPackage);

        Iterable<PluginPackage> allPluginPackages = pluginPackageRepository.findAll();
        assertThat(allPluginPackages).isNotNull();
        assertThat(allPluginPackages).contains(pluginPackage);
    }

    @Test
    public void findLatestPluginPackageByName() {
        PluginPackage pluginPackageV1 = mockPluginPackage("mockPluginPackage-findLatestPluginPackageByName", "v1");
        PluginPackage pluginPackageV2 = mockPluginPackage("mockPluginPackage-findLatestPluginPackageByName", "v10.0");
        PluginPackage pluginPackageV3 = mockPluginPackage("mockPluginPackage-findLatestPluginPackageByName", "V9.1");
        pluginPackageRepository.saveAll(newArrayList(pluginPackageV1, pluginPackageV2, pluginPackageV3));

        Optional<PluginPackage> latestVersion = pluginPackageRepository.findLatestVersionByName("mockPluginPackage-findLatestPluginPackageByName");
        assertThat(latestVersion).isPresent();
        assertThat(latestVersion.get().getVersion()).isEqualTo("v10.0");

        latestVersion = pluginPackageRepository.findLatestVersionByName("mockPluginPackage-findLatestPluginPackageByName", "v10.0");
        assertThat(latestVersion).isPresent();
        assertThat(latestVersion.get().getVersion()).isEqualTo("V9.1");
    }

    @Test
    public void findPluginConfigInterfacesByConfigId() {
        PluginPackage pluginPackage = mockPluginPackage("mockPluginPackage", "v1");
        pluginPackageRepository.save(pluginPackage);

        PluginConfig pluginConfig = pluginPackage.getPluginConfigs().get(0);
        List<PluginConfigInterface> interfaces = pluginConfigRepository.findAllPluginConfigInterfacesByConfigIdAndFetchParameters(pluginConfig.getId());

        assertThat(interfaces).containsExactlyElementsOf(pluginConfig.getInterfaces());
    }

    @Test
    public void findMaxPortByHost() {
        PluginPackage pluginPackage = new PluginPackage(null, "test-findSavedInstanceByContainerId", "v1", "qcloud.image", "wecube-plugin", "201904191234", "8080", "/home/app/conf", "/home/app/log", null, null, null, null);
        PluginConfig pluginConfig = new PluginConfig(null, pluginPackage, "VM", null, NOT_CONFIGURED, null, null);

        pluginPackage.setPluginConfigs(newArrayList(pluginConfig));
        pluginPackageRepository.save(pluginPackage);

        PluginInstance pluginInstance = new PluginInstance(null, pluginPackage, "test-instance-container-id", "localhost", 29999, "running");
        pluginInstanceRepository.save(pluginInstance);

        Integer foundPluginInstancePort = pluginInstanceRepository.findMaxPortByHost("localhost");
        assertThat(foundPluginInstancePort).isEqualTo(pluginInstance.getPort());
    }

    @Test
    public void findByPackageIdAndStatus() {
        PluginPackage pluginPackage = new PluginPackage(null, "test-findSavedInstanceByContainerId", "v1", "qcloud.image", "wecube-plugin", "201904191234", "8080", "/home/app/conf", "/home/app/log", null, null, null, null);
        PluginConfig pluginConfig = new PluginConfig(null, pluginPackage, "VM", null, NOT_CONFIGURED, null, null);
        pluginPackage.setPluginConfigs(newArrayList(pluginConfig));
        pluginPackageRepository.save(pluginPackage);

        PluginInstance pluginInstance = new PluginInstance(null, pluginPackage, "test-instance-container-id", "localhost", 20000, "RUNNING");
        pluginInstanceRepository.save(pluginInstance);

        List<PluginInstance> pluginInstances = pluginInstanceRepository.findByStatusAndPackageId("RUNNING", pluginPackage.getId());
        assertThat(pluginInstances.get(0).getInstanceContainerId()).isEqualTo(pluginInstance.getInstanceContainerId());
    }

    private PluginPackage mockPluginPackage(String name, String version) {
        PluginPackage mockPluginPackage = new PluginPackage(null, name, version, "qcloud.image", "wecube-plugin", "201904191234", "8080", "/home/app/conf", "/home/app/log", null, null, newArrayList(), newArrayList());
        PluginConfig mockPlugin = new PluginConfig(null, mockPluginPackage, "mockPlugin", null, NOT_CONFIGURED, newArrayList(), newArrayList());
        mockPlugin.addPluginConfigInterface(mockPluginConfigInterface(mockPlugin));
        mockPluginPackage.addPluginConfig(mockPlugin);

        return mockPluginPackage;
    }

    private PluginConfigInterface mockPluginConfigInterface(PluginConfig pluginConfig) {
        PluginConfigInterface pluginConfigInterface = new PluginConfigInterface(null, pluginConfig, "create", "'create", "Qcloud_vpc_create", "/v1/qcloud/vpc/create", null, null, null, newLinkedHashSet(), newLinkedHashSet());
        pluginConfigInterface.addInputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface, TYPE_INPUT, "provider_params", "string", null, null, null, null, null));
        pluginConfigInterface.addInputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface, TYPE_INPUT, "name", "string", null, null, null, null, null));
        pluginConfigInterface.addOutputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface, TYPE_OUTPUT, "id", "string", null, null, null, null, null));
        return pluginConfigInterface;
    }

}
