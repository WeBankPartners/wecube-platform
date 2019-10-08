package com.webank.wecube.core.jpa;

import com.google.common.collect.Iterables;
import com.webank.wecube.core.DatabaseBasedTest;
import com.webank.wecube.core.domain.plugin.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.core.domain.plugin.PluginConfig.Status.NOT_CONFIGURED;
import static com.webank.wecube.core.domain.plugin.PluginConfigInterfaceParameter.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;

public class PluginRepositoryIntegrationTest extends DatabaseBasedTest {

    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginConfigRepository pluginConfigRepository;
    @Autowired
    PluginInstanceRepository pluginInstanceRepository;
    @Autowired
    PluginModelEntityRepository pluginModelEntityRepository;
    @Autowired
    PluginModelAttributeRepository pluginModelAttributeRepository;

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

    @Test
    public void findAllPluginModelEntities() {
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        List<PluginModelEntity> pluginModelEntityList = mockPluginModelEntityList(package_1);
        pluginModelEntityRepository.saveAll(pluginModelEntityList);
        Iterable<PluginPackage> pluginPackageResult = pluginPackageRepository.findAll();
        Iterable<PluginModelEntity> pluginModelEntityResult = pluginModelEntityRepository.findAll();
        assertThat(pluginModelEntityRepository.findAll()).isEqualTo(pluginModelEntityList);
    }

    @Test
    public void findAllPluginModelEntityWithAttribute() {
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        List<PluginModelEntity> pluginModelEntityList = mockPluginModelEntityList(package_1);
        mockPluginModelEntityListWithAttributeList(pluginModelEntityList);
        pluginModelEntityRepository.saveAll(pluginModelEntityList);
        Iterable<PluginPackage> pluginPackageResult = pluginPackageRepository.findAll();
        Iterable<PluginModelEntity> pluginEntityResult = pluginModelEntityRepository.findAll();
        Iterable<PluginModelAttribute> pluginModelAttributeResult = pluginModelAttributeRepository.findAll();
        assertThat(pluginModelEntityRepository.findAll()).isEqualTo(pluginModelEntityList);
    }

    @Test
    public void deleteEntity() {
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        List<PluginModelEntity> pluginModelEntityList = mockPluginModelEntityList(package_1);
        mockPluginModelEntityListWithAttributeList(pluginModelEntityList);
        pluginModelEntityRepository.saveAll(pluginModelEntityList);
        assertThat(Iterables.size(pluginPackageRepository.findAllByName("package_1"))).isEqualTo(1);
        assertThat(Iterables.size(pluginModelEntityRepository.findAll())).isEqualTo(3);
        assertThat(Iterables.size(pluginModelAttributeRepository.findAll())).isEqualTo(15);
//
        // delete the package
        pluginModelEntityRepository.deleteAll();
        pluginPackageRepository.deleteAll();
        assertThat(Iterables.size(pluginPackageRepository.findAll())).isEqualTo(0);
        assertThat(Iterables.size(pluginModelEntityRepository.findAll())).isEqualTo(0);
        assertThat(Iterables.size(pluginModelAttributeRepository.findAll())).isEqualTo(0);
    }

    @Test
    public void dataModelStateTest() {
        // when correct state code is declared
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        PluginModelEntity pluginModelEntity_1 = new PluginModelEntity(package_1, "entity_1", "entity_1", "entity_1_description", "draft");
        PluginModelAttribute pluginModelAttribute_1 = new PluginModelAttribute(pluginModelEntity_1, null, "attribute_1", "attribute_1_description", "string", "draft");
        assertThat(pluginPackageRepository.save(package_1)).isEqualTo(pluginPackageRepository.findAll().iterator().next());


        // when wrong state code is declared
        PluginPackage package_2 = mockPluginPackage("package_1", "1.0");
        String fail_code = "should_failed";
        try {
            PluginModelEntity pluginModelEntity_2 = new PluginModelEntity(package_2, "entity_1", "entity_1", "entity_1_description", fail_code);
            PluginModelAttribute pluginModelAttribute_2 = new PluginModelAttribute(pluginModelEntity_2, null, "attribute_1", "attribute_1_description", "string", fail_code);
        } catch (IllegalArgumentException argException) {
            assertThat(argException.getMessage()).isEqualTo(String.format("Cannot find the data model state from code %s", fail_code));
        }

    }

    @Test
    public void dataModelDataTypeTest() {
        // when correct state code is declared
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        PluginModelEntity pluginModelEntity_1 = new PluginModelEntity(package_1, "entity_1", "entity_1", "entity_1_description", "draft");
        PluginModelAttribute pluginModelAttribute_1 = new PluginModelAttribute(pluginModelEntity_1, null, "attribute_1", "attribute_1_description", "string", "draft");
        assertThat(pluginPackageRepository.save(package_1)).isEqualTo(pluginPackageRepository.findAll().iterator().next());


        // when wrong state code is declared
        PluginPackage package_2 = mockPluginPackage("package_1", "1.0");
        String fail_code = "should_failed";
        try {
            PluginModelEntity pluginModelEntity_2 = new PluginModelEntity(package_2, "entity_1", "entity_1", "entity_1_description", "draft");
            PluginModelAttribute pluginModelAttribute_2 = new PluginModelAttribute(pluginModelEntity_2, null, "attribute_1", "attribute_1_description", fail_code, "draft");
        } catch (IllegalArgumentException argException) {
            assertThat(argException.getMessage()).isEqualTo(String.format("Cannot find the data model data type from code %s", fail_code));
        }
    }


    public static PluginPackage mockPluginPackage(String name, String version) {
        PluginPackage mockPluginPackage = new PluginPackage(null, name, version, "qcloud.image", "wecube-plugin", "201904191234", "8080", "/home/app/conf", "/home/app/log", null, null, newArrayList(), newArrayList());
        PluginConfig mockPlugin = new PluginConfig(null, mockPluginPackage, "mockPlugin", null, NOT_CONFIGURED, newArrayList(), newArrayList());
        mockPlugin.addPluginConfigInterface(mockPluginConfigInterface(mockPlugin));
        mockPluginPackage.addPluginConfig(mockPlugin);

        return mockPluginPackage;
    }

    public static PluginConfigInterface mockPluginConfigInterface(PluginConfig pluginConfig) {
        PluginConfigInterface pluginConfigInterface = new PluginConfigInterface(null, pluginConfig, "create", "'create", "Qcloud_vpc_create", "/v1/qcloud/vpc/create", null, null, null, newLinkedHashSet(), newLinkedHashSet());
        pluginConfigInterface.addInputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface, TYPE_INPUT, "provider_params", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, null, null, null, null));
        pluginConfigInterface.addInputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface, TYPE_INPUT, "name", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, null, null, null, null));
        pluginConfigInterface.addOutputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface, TYPE_OUTPUT, "id", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, null, null, null, null));
        return pluginConfigInterface;
    }

    public static List<PluginModelEntity> mockPluginModelEntityList(PluginPackage pluginPackage) {
        List<PluginModelEntity> pluginModelEntityList = new ArrayList<>();
        pluginModelEntityList.add(new PluginModelEntity(pluginPackage, "entity_1", "entity_1", "entity_1_description", "draft"));
        pluginModelEntityList.add(new PluginModelEntity(pluginPackage, "entity_2", "entity_2", "entity_2_description", "draft"));
        pluginModelEntityList.add(new PluginModelEntity(pluginPackage, "entity_3", "entity_3", "entity_3_description", "draft"));
        return pluginModelEntityList;
    }

    public static void mockPluginModelEntityListWithAttributeList(List<PluginModelEntity> pluginModelEntityList) {
        for (PluginModelEntity pluginModelEntity : pluginModelEntityList) {
            PluginModelAttribute attribute_1 = new PluginModelAttribute(pluginModelEntity, null, "attribute_1", "attribute_1_description", "string", "draft");
            PluginModelAttribute attribute_2 = new PluginModelAttribute(pluginModelEntity, null, "attribute_2", "attribute_2_description", "string", "draft");
            PluginModelAttribute attribute_3 = new PluginModelAttribute(pluginModelEntity, attribute_1, "attribute_3", "attribute_3_description", "ref", "draft");
            PluginModelAttribute attribute_4 = new PluginModelAttribute(pluginModelEntity, attribute_1, "attribute_4", "attribute_4_description", "ref", "draft");
            PluginModelAttribute attribute_5 = new PluginModelAttribute(pluginModelEntity, attribute_2, "attribute_5", "attribute_5_description", "ref", "draft");
            pluginModelEntity.setPluginModelAttributeList(new ArrayList<>(Arrays.asList(attribute_1, attribute_2, attribute_3, attribute_4, attribute_5)));
        }
    }

}
