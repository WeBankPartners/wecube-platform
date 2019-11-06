package com.webank.wecube.platform.core.jpa;

import com.google.common.collect.Iterables;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.*;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;
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
    PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    PluginPackageAttributeRepository pluginPackageAttributeRepository;

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

        Optional<PluginPackage> latestVersion = pluginPackageRepository
                .findLatestVersionByName("mockPluginPackage-findLatestPluginPackageByName");
        assertThat(latestVersion).isPresent();
        assertThat(latestVersion.get().getVersion()).isEqualTo("v10.0");

        latestVersion = pluginPackageRepository
                .findLatestVersionByName("mockPluginPackage-findLatestPluginPackageByName", "v10.0");
        assertThat(latestVersion).isPresent();
        assertThat(latestVersion.get().getVersion()).isEqualTo("V9.1");
    }

    @Test
    public void findPluginConfigInterfacesByConfigId() {
        PluginPackage pluginPackage = mockPluginPackage("mockPluginPackage", "v1");
        pluginPackageRepository.save(pluginPackage);

        PluginConfig pluginConfig = pluginPackage.getPluginConfigs().iterator().next();
        List<PluginConfigInterface> interfaces = pluginConfigRepository
                .findAllPluginConfigInterfacesByConfigIdAndFetchParameters(pluginConfig.getId());

        assertThat(interfaces).containsExactlyElementsOf(pluginConfig.getInterfaces());
    }

    @Test
    public void findAllPluginPackageEntities() {
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        List<PluginPackageEntity> pluginPackageEntityList = mockPluginPackageEntityList(package_1);
        pluginPackageEntityRepository.saveAll(pluginPackageEntityList);
        assertThat(pluginPackageEntityRepository.findAll()).isEqualTo(pluginPackageEntityList);
    }

    @Test
    public void findAllPluginPackageEntityWithAttribute() {
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        List<PluginPackageEntity> pluginPackageEntityList = mockPluginPackageEntityList(package_1);
        mockPluginPackageEntityListWithAttributeList(pluginPackageEntityList);
        pluginPackageEntityRepository.saveAll(pluginPackageEntityList);
        assertThat(pluginPackageEntityRepository.findAll()).isEqualTo(pluginPackageEntityList);
    }

    @Test
    public void deleteEntity() {
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        List<PluginPackageEntity> pluginPackageEntityList = mockPluginPackageEntityList(package_1);
        mockPluginPackageEntityListWithAttributeList(pluginPackageEntityList);
        pluginPackageEntityRepository.saveAll(pluginPackageEntityList);
        assertThat(Iterables.size(pluginPackageRepository.findAllByName("package_1"))).isEqualTo(1);
        assertThat(Iterables.size(pluginPackageEntityRepository.findAll())).isEqualTo(3);
        assertThat(Iterables.size(pluginPackageAttributeRepository.findAll())).isEqualTo(15);
//
        // delete the package
        pluginPackageEntityRepository.deleteAll();
        pluginPackageRepository.deleteAll();
        assertThat(Iterables.size(pluginPackageRepository.findAll())).isEqualTo(0);
        assertThat(Iterables.size(pluginPackageEntityRepository.findAll())).isEqualTo(0);
        assertThat(Iterables.size(pluginPackageAttributeRepository.findAll())).isEqualTo(0);
    }

    @Test
    public void dataModelDataTypeTest() {
        // when correct data type is declared
        PluginPackage package_1 = mockPluginPackage("package_1", "1.0");
        PluginPackageEntity pluginPackageEntity_1 = new PluginPackageEntity(package_1.getPluginPackageDataModel(), "entity_1", "entity_1",
                "entity_1_description");
        PluginPackageAttribute pluginPackageAttribute_1 = new PluginPackageAttribute(pluginPackageEntity_1, null,
                "attribute_1", "attribute_1_description", "str");
        assertThat(pluginPackageRepository.save(package_1))
                .isEqualTo(pluginPackageRepository.findAll().iterator().next());

        // when wrong data type is declared
        PluginPackage package_2 = mockPluginPackage("package_1", "1.0");
        String fail_code = "should_failed";
        try {
            PluginPackageEntity pluginPackageEntity_2 = new PluginPackageEntity(package_2.getPluginPackageDataModel(), "entity_1", "entity_1",
                    "entity_1_description");
            PluginPackageAttribute pluginPackageAttribute_2 = new PluginPackageAttribute(pluginPackageEntity_2, null,
                    "attribute_1", "attribute_1_description", fail_code);
        } catch (IllegalArgumentException argException) {
            assertThat(argException.getMessage())
                    .isEqualTo(String.format("Cannot find the data model data type from code %s", fail_code));
        }
    }

    public static PluginPackage mockPluginPackage(String name, String version) {
        PluginPackage mockPluginPackage = new PluginPackage(null, name, version, UNREGISTERED, new Timestamp(System.currentTimeMillis()), false,
                newLinkedHashSet(), newLinkedHashSet(), null, newLinkedHashSet(),
                newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet(), newLinkedHashSet());
        PluginConfig mockPlugin = new PluginConfig(null, mockPluginPackage, "mockPlugin", null, "mockEntity",
                PluginConfig.Status.DISABLED, newLinkedHashSet());
        mockPlugin.addPluginConfigInterface(mockPluginConfigInterface(mockPlugin));
        mockPluginPackage.addPluginConfig(mockPlugin);

        Long now = System.currentTimeMillis();
        PluginPackageDataModel mockPluginPackageDataModel = new PluginPackageDataModel(null, null, mockPluginPackage.getName(), false, null, null, , now, null);
        mockPluginPackage.setPluginPackageDataModel(mockPluginPackageDataModel);

        return mockPluginPackage;
    }

    public static PluginConfigInterface mockPluginConfigInterface(PluginConfig pluginConfig) {
        PluginConfigInterface pluginConfigInterface = new PluginConfigInterface(null, pluginConfig, "create", "'create",
                "Qcloud_vpc_create", "/v1/qcloud/vpc/create", "POST", newLinkedHashSet(), newLinkedHashSet());
        pluginConfigInterface.addInputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_INPUT, "provider_params", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y"));
        pluginConfigInterface.addInputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_INPUT, "name", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y"));
        pluginConfigInterface.addOutputParameter(new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_OUTPUT, "id", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y"));
        return pluginConfigInterface;
    }

    public static List<PluginPackageEntity> mockPluginPackageEntityList(PluginPackage pluginPackage) {
        List<PluginPackageEntity> pluginPackageEntityList = new ArrayList<>();
        pluginPackageEntityList
                .add(new PluginPackageEntity(pluginPackage.getPluginPackageDataModel(), "entity_1", "entity_1", "entity_1_description"));
        pluginPackageEntityList
                .add(new PluginPackageEntity(pluginPackage.getPluginPackageDataModel(), "entity_2", "entity_2", "entity_2_description"));
        pluginPackageEntityList
                .add(new PluginPackageEntity(pluginPackage.getPluginPackageDataModel(), "entity_3", "entity_3", "entity_3_description"));
        return pluginPackageEntityList;
    }

    public static void mockPluginPackageEntityListWithAttributeList(List<PluginPackageEntity> pluginPackageEntityList) {
        for (PluginPackageEntity pluginPackageEntity : pluginPackageEntityList) {
            PluginPackageAttribute attribute_1 = new PluginPackageAttribute(pluginPackageEntity, null, "attribute_1",
                    "attribute_1_description", "str");
            PluginPackageAttribute attribute_2 = new PluginPackageAttribute(pluginPackageEntity, null, "attribute_2",
                    "attribute_2_description", "str");
            PluginPackageAttribute attribute_3 = new PluginPackageAttribute(pluginPackageEntity, attribute_1,
                    "attribute_3", "attribute_3_description", "ref");
            PluginPackageAttribute attribute_4 = new PluginPackageAttribute(pluginPackageEntity, attribute_1,
                    "attribute_4", "attribute_4_description", "ref");
            PluginPackageAttribute attribute_5 = new PluginPackageAttribute(pluginPackageEntity, attribute_2,
                    "attribute_5", "attribute_5_description", "ref");
            pluginPackageEntity.setPluginPackageAttributeList(
                    new ArrayList<>(Arrays.asList(attribute_1, attribute_2, attribute_3, attribute_4, attribute_5)));
        }
    }

}
