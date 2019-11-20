package com.webank.wecube.platform.core.jpa;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.*;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.*;

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
    PluginPackageDataModelRepository dataModelRepository;
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
        pluginPackageRepository.save(package_1);
        PluginPackageDataModel dataModel = mockPluginPackageDataModel(package_1);
        dataModelRepository.save(dataModel);
        assertThat(Iterables.size(pluginPackageRepository.findAllByName("package_1"))).isEqualTo(1);
        assertThat(Iterables.size(dataModelRepository.findAll())).isEqualTo(1);
        assertThat(Iterables.size(pluginPackageEntityRepository.findAll())).isEqualTo(3);
        assertThat(Iterables.size(pluginPackageAttributeRepository.findAll())).isEqualTo(15);


        dataModelRepository.deleteAll();
        pluginPackageRepository.deleteAll();
        assertThat(Iterables.size(pluginPackageRepository.findAll())).isEqualTo(0);
        assertThat(Iterables.size(dataModelRepository.findAll())).isEqualTo(0);
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
        mockPlugin.setInterfaces(newLinkedHashSet(mockPluginConfigInterface(mockPlugin)));
        mockPluginPackage.addPluginConfig(mockPlugin);

        Long now = System.currentTimeMillis();
        PluginPackageDataModel mockPluginPackageDataModel = new PluginPackageDataModel(null, 1, mockPluginPackage.getName(), false, null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), now, null);
        mockPluginPackage.setPluginPackageDataModel(mockPluginPackageDataModel);

        return mockPluginPackage;
    }

    public static PluginConfigInterface mockPluginConfigInterface(PluginConfig pluginConfig) {
        PluginConfigInterface pluginConfigInterface = new PluginConfigInterface(null, pluginConfig, "create", "'create",
                "Qcloud_vpc_create", "/v1/qcloud/vpc/create", "POST", newLinkedHashSet(), newLinkedHashSet());
        PluginConfigInterfaceParameter inputParameter = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_INPUT, "provider_params", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        PluginConfigInterfaceParameter inputParameter2 = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_INPUT, "name", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        pluginConfigInterface.setInputParameters(Sets.newHashSet(inputParameter, inputParameter2));
        PluginConfigInterfaceParameter outputParameter = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_OUTPUT, "id", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        pluginConfigInterface.setOutputParameters(Sets.newHashSet(outputParameter));
        return pluginConfigInterface;
    }

    public static PluginPackageDataModel mockPluginPackageDataModel(PluginPackage pluginPackage) {
        long now = System.currentTimeMillis();
        PluginPackageDataModel mockPluginPackageDataModel = new PluginPackageDataModel(null, 1, pluginPackage.getName(), false, null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), now, null);
        mockPluginPackageDataModel.setPluginPackageEntities(mockPluginPackageEntityList(mockPluginPackageDataModel));

        return mockPluginPackageDataModel;
    }

    public static Set<PluginPackageEntity> mockPluginPackageEntityList(PluginPackageDataModel dataModel) {
        Set<PluginPackageEntity> pluginPackageEntities = newLinkedHashSet();
        pluginPackageEntities
                .add(new PluginPackageEntity(dataModel, "entity_1", "entity_1", "entity_1_description"));
        pluginPackageEntities
                .add(new PluginPackageEntity(dataModel, "entity_2", "entity_2", "entity_2_description"));
        pluginPackageEntities
                .add(new PluginPackageEntity(dataModel, "entity_3", "entity_3", "entity_3_description"));
        mockPluginPackageEntityListWithAttributes(pluginPackageEntities);
        return pluginPackageEntities;
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

    public static void mockPluginPackageEntityListWithAttributes(Set<PluginPackageEntity> pluginPackageEntityList) {
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
