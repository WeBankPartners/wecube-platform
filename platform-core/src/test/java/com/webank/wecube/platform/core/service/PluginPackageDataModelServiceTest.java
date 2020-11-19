package com.webank.wecube.platform.core.service;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.MAPPING_TYPE_CMDB_CI_TYPE;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.TYPE_INPUT;
import static com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter.TYPE_OUTPUT;
import static com.webank.wecube.platform.core.domain.plugin.PluginPackage.Status.UNREGISTERED;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.assertj.core.util.Sets;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.jpa.PluginPackageAttributeRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import com.webank.wecube.platform.core.service.plugin.PluginPackageDataModelService;

@Ignore
public class PluginPackageDataModelServiceTest extends DatabaseBasedTest {
    public static final int NON_EXIST_PACKAGE_ID = 9999;
    private static final String NON_EXIST_PACKAGE_NAME = "this-is-a-non-exist-package-name";
    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    PluginPackageAttributeRepository pluginPackageAttributeRepository;
    @Autowired
    PluginPackageDataModelService pluginPackageDataModelService;

    Integer MOCK_SIZE_PER_PACKAGE = 3;
    Integer PACKAGE_SIZE = 4;

    @Test
    public void whenRegisterDataModelShouldSuccess() {
        // register two packages with same name but different versions and their
        // data models
        PluginPackageDataModelDto pluginPackageDataModelDto = mockPluginPackageDataModelDto("Package_1", "1.0");
        PluginPackageDataModelDto returnedPluginPackageDataModelDto = pluginPackageDataModelService
                .register(pluginPackageDataModelDto);
        Iterable<PluginPackageEntity> foundAllRegisteredEntityList = pluginPackageEntityRepository.findAll();
        assertThat(Iterators.size(foundAllRegisteredEntityList.iterator()))
                .isEqualTo(returnedPluginPackageDataModelDto.getPluginPackageEntities().size());

        PluginPackageDataModelDto pluginPackageDataModelDto2 = mockPluginPackageDataModelDto("Package_1", "2.0");
        Iterable<PluginPackageEntity> foundAllRegisteredEntityList2 = pluginPackageEntityRepository.findAll();
        assertThat(Iterators.size(foundAllRegisteredEntityList2.iterator())).isEqualTo(MOCK_SIZE_PER_PACKAGE * 2);

        // data model with false info
        PluginPackageDataModelDto pluginPackageDataModelDto3 = mockFalsePluginPackageDataModelDto("Package_2", "2.0");
        try {
            pluginPackageDataModelService.register(pluginPackageDataModelDto3);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage().contains("Cannot found the specified data model with package name"))
                    .isEqualTo(true);
        }

        // data model reference to a registered package with one attribute refer
        // to package1`entity_1
        pluginPackageRepository.save(mockPluginPackage("Package_3", "1.0"));
        PluginPackageDataModelDto pluginPackageDataModelDto4 = mockPluginPackageDataModelDtoByRegisteredPackage(
                "Package_3");
        PluginPackageDataModelDto registeredPluginPackageDataModelDto4 = pluginPackageDataModelService
                .register(pluginPackageDataModelDto4);

        Iterable<PluginPackageEntity> all = pluginPackageEntityRepository.findAll();
        assertThat(registeredPluginPackageDataModelDto4.getPluginPackageEntities().size())
                .isEqualTo(pluginPackageDataModelDto4.getPluginPackageEntities().size());

        // data model reference to a registered package with no attribute
        // reference
        pluginPackageRepository.save(mockPluginPackage("Package_4", "1.0"));
        PluginPackageDataModelDto pluginPackageDataModelDto5 = mockPluginPackageDataModelDtoWithNoAttrRef("Package_4",
                1);
        PluginPackageDataModelDto registeredPluginPackageDataModelDto5 = pluginPackageDataModelService
                .register(pluginPackageDataModelDto5);
        assertThat(registeredPluginPackageDataModelDto5.getPluginPackageEntities().size())
                .isEqualTo(pluginPackageDataModelDto5.getPluginPackageEntities().size());

        // test current package number and data model size
        Iterable<PluginPackage> allPackages = pluginPackageRepository.findAll();
        assertThat(Iterables.size(allPackages)).isEqualTo(PACKAGE_SIZE + 1); // because
                                                                             // in
                                                                             // mock
                                                                             // false
                                                                             // function,
                                                                             // the
                                                                             // package
                                                                             // is
                                                                             // successfully
                                                                             // saved
        Iterable<PluginPackageEntity> allDataModels = pluginPackageEntityRepository.findAll();
        assertThat(Iterables.size(allDataModels)).isEqualTo(PACKAGE_SIZE * MOCK_SIZE_PER_PACKAGE);
    }

    // @Test
    // public void whenRegisterSamePackageShouldSuccess() {
    // List<PluginPackageEntityDto> pluginPackageEntityDtoList =
    // mockPluginPackageEntityDtoList("Package_1", "1.0");
    // pluginPackageDataModelService.register(pluginPackageEntityDtoList);
    // pluginPackageDataModelService.register(pluginPackageEntityDtoList);
    //
    // Iterable<PluginPackageEntity> allEntities =
    // pluginPackageEntityRepository.findAll();
    // assertThat(Iterables.size(allEntities)).isEqualTo(MOCK_SIZE_PER_PACKAGE *
    // 2);
    //
    // Optional<List<PluginPackageEntity>> foundLatestByPackageNameOpt =
    // pluginPackageEntityRepository.findAllLatestEntityByPluginPackage_name("Package_1");
    // if (!foundLatestByPackageNameOpt.isPresent()) {
    // fail("Cannot find entities");
    // }
    // List<PluginPackageEntity> pluginPackageEntities =
    // foundLatestByPackageNameOpt.get();
    // assertThat(pluginPackageEntities.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE);
    //
    // System.out.println(foundLatestByPackageNameOpt);
    // }

    @Test
    public void whenOverviewShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        Set<PluginPackageDataModelDto> registeredAllDataModelList = pluginPackageDataModelService.overview();
        assertThat(registeredAllDataModelList.size()).isEqualTo(PACKAGE_SIZE - 1); // because
                                                                                   // the
                                                                                   // package
                                                                                   // 2
                                                                                   // hasn't
                                                                                   // been
                                                                                   // registered
                                                                                   // to
                                                                                   // database
        registeredAllDataModelList
                .forEach(registeredDataModel -> assertThat(registeredDataModel.getPluginPackageEntities().size())
                        .isEqualTo(MOCK_SIZE_PER_PACKAGE));
        registeredAllDataModelList.forEach(registeredDataModel -> registeredDataModel.getPluginPackageEntities()
                .forEach(entity -> assertThat(entity.getAttributes().size()).isEqualTo(3)));
    }

    @Test
    public void whenPackageViewByPackageIdShouldSuccess() {
        PluginPackageDataModelDto pluginPackageDataModelDto = mockPluginPackageDataModelDto("Package_1", "1.0");
        pluginPackageDataModelService.register(pluginPackageDataModelDto);
        PluginPackageDataModelDto foundEntityDtoListByPackageNameAndVersion = pluginPackageDataModelService
                .packageView("Package_1");
        assertThat(foundEntityDtoListByPackageNameAndVersion.getPluginPackageEntities().size())
                .isEqualTo(MOCK_SIZE_PER_PACKAGE);
        assertThat(foundEntityDtoListByPackageNameAndVersion.getPackageName()).isEqualTo("Package_1");
        assertThat(foundEntityDtoListByPackageNameAndVersion.getVersion()).isEqualTo(1);
        assertThat(foundEntityDtoListByPackageNameAndVersion.getPluginPackageEntities()).hasSize(3);
    }

    @Test
    public void givenPackageNotExistWhenQueryDataModelThenReturnSuccessNull() {
        PluginPackageDataModelDto foundEntityDtoListByPackageNameAndVersion = pluginPackageDataModelService
                .packageView(NON_EXIST_PACKAGE_NAME);
        assertThat(foundEntityDtoListByPackageNameAndVersion).isNull();
    }

    @Test
    public void whenGetRefByInfoShouldSucceed() {
        int REF_BY_COUNT = 4;
        pluginPackageDataModelService.register(mockPluginPackageDataModelDtoWithIdAsAttribute("Package_1", "1.0"));
        pluginPackageDataModelService.register(mockPluginPackageDataModelDtoWithIdAsAttribute("Package_2", "1.0"));
        pluginPackageDataModelService.register(mockPluginPackageDataModelDtoWithIdAsAttribute("Package_3", "1.0"));
        assertThat(pluginPackageDataModelService.getRefByInfo("Package_1", "Entity_1").size()).isEqualTo(REF_BY_COUNT);
        assertThat(pluginPackageDataModelService.getRefByInfo("Package_2", "Entity_1").size()).isEqualTo(REF_BY_COUNT);
        assertThat(pluginPackageDataModelService.getRefByInfo("Package_3", "Entity_1").size()).isEqualTo(REF_BY_COUNT);
    }

    @Test
    public void entityViewShouldSucceed() {
        final String ENTITY_NAME = "Entity_1";
        PluginPackageDataModelDto dataModelDto = mockPluginPackageDataModelDto("PackageName", "1.0");
        this.pluginPackageDataModelService.register(dataModelDto);
        List<PluginPackageAttributeDto> pluginPackageAttributeDtos = this.pluginPackageDataModelService
                .entityView("PackageName", ENTITY_NAME);
        assertThat(pluginPackageAttributeDtos.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE);
        pluginPackageAttributeDtos
                .forEach(pluginPackageAttributeDto -> assertThat(pluginPackageAttributeDto.getEntityName())
                        .isEqualTo(ENTITY_NAME));

    }

    private PluginPackageDataModelDto mockPluginPackageDataModelDto(String packageName, String packageVersion) {

        Set<PluginPackageEntityDto> pluginPackageEntityDtos = newLinkedHashSet();
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage(packageName, packageVersion);
        pluginPackageRepository.save(package_1);

        Long now = System.currentTimeMillis();
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto(null, 1, package_1.getName(), false,
                null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), now, null);

        // mock the entityDto list with nested attribute List inside

        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(dataModelDto.getVersion());
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i >= 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName(packageName);
                    attributeDto.setRefEntityName("Entity_1");
                    attributeDto.setRefAttributeName("Attribute_1");
                }
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtos.add(entityDto);
        }
        dataModelDto.setPluginPackageEntities(pluginPackageEntityDtos);

        return dataModelDto;
    }

    private PluginPackageDataModelDto mockFalsePluginPackageDataModelDto(String packageName, String packageVersion) {

        Set<PluginPackageEntityDto> pluginPackageEntityDtos = newLinkedHashSet();
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage(packageName, packageVersion);
        pluginPackageRepository.save(package_1);

        Long now = System.currentTimeMillis();
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto(null, 1, package_1.getName(), false,
                null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), now, null);

        // mock the entityDto list with nested attribute List inside

        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(dataModelDto.getVersion());
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i >= 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName("falsePackageName");
                    attributeDto.setRefEntityName("falseEntityName");
                    attributeDto.setRefAttributeName("falseAttributeName");
                }
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtos.add(entityDto);
        }
        dataModelDto.setPluginPackageEntities(pluginPackageEntityDtos);

        return dataModelDto;
    }

    private List<PluginPackageEntityDto> mockPluginPackageEntityDtoList(String packageName, String packageVersion) {

        List<PluginPackageEntityDto> pluginPackageEntityDtoList = new ArrayList<>();
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage(packageName, packageVersion);
        pluginPackageRepository.save(package_1);

        // mock the entityDto list with nested attribute List inside

        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(package_1.getPluginPackageDataModel().getVersion());
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i >= 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName(packageName);
                    attributeDto.setRefEntityName("Entity_1");
                    attributeDto.setRefAttributeName("Attribute_1");
                }
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtoList.add(entityDto);
        }

        return pluginPackageEntityDtoList;
    }

    private PluginPackageDataModelDto mockPluginPackageDataModelDtoByRegisteredPackage(String packageName) {

        Set<PluginPackageEntityDto> pluginPackageEntityDtos = newLinkedHashSet();
        Long now = System.currentTimeMillis();
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto(null, 1, packageName, false, null, null,
                PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), now, null);

        // mock the entityDto list with nested attribute List inside
        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(dataModelDto.getVersion());
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i >= 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName("Package_1");
                    attributeDto.setRefEntityName("Entity_1");
                    attributeDto.setRefAttributeName("Attribute_1");
                }
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtos.add(entityDto);
        }
        dataModelDto.setPluginPackageEntities(pluginPackageEntityDtos);

        return dataModelDto;
    }

    private List<PluginPackageEntityDto> mockPluginPackageEntityDtoListByRegisteredPackage(String packageName,
            Integer dataModelVersion) {

        List<PluginPackageEntityDto> pluginPackageEntityDtoList = new ArrayList<>();

        // mock the entityDto list with nested attribute List inside
        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(dataModelVersion);
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i >= 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName("Package_1");
                    attributeDto.setRefEntityName("Entity_1");
                    attributeDto.setRefAttributeName("Attribute_1");
                }
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtoList.add(entityDto);
        }

        return pluginPackageEntityDtoList;
    }

    private PluginPackageDataModelDto mockPluginPackageDataModelDtoWithNoAttrRef(String packageName,
            Integer dataModelVersion) {
        Set<PluginPackageEntityDto> pluginPackageEntityDtos = newLinkedHashSet();
        Long now = System.currentTimeMillis();
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto(null, 1, packageName, false, null, null,
                PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), now, null);

        // mock the entityDto list with nested attribute List inside
        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(dataModelVersion);
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtos.add(entityDto);
        }
        dataModelDto.setPluginPackageEntities(pluginPackageEntityDtos);

        return dataModelDto;
    }

    private List<PluginPackageEntityDto> mockPluginPackageEntityDtoListWithNoAttrRef(String packageName,
            Integer dataModelVersion) {

        List<PluginPackageEntityDto> pluginPackageEntityDtoList = new ArrayList<>();

        // mock the entityDto list with nested attribute List inside
        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(dataModelVersion);
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtoList.add(entityDto);
        }

        return pluginPackageEntityDtoList;
    }

    private List<PluginPackageEntityDto> mockFalsePluginPackageEntityDtoList(String packageName, String packageVersion,
            String referenceVersion) {

        List<PluginPackageEntityDto> pluginPackageEntityDtoList = new ArrayList<>();
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage(packageName, packageVersion);
        pluginPackageRepository.save(package_1);
        // mock the entityDto list with nested attribute List inside

        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(package_1.getPluginPackageDataModel().getVersion());
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i > 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName("faultPackageName");
                    attributeDto.setRefEntityName("faultEntityName");
                    attributeDto.setRefAttributeName("faultAttributeName");
                }
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtoList.add(entityDto);
        }

        return pluginPackageEntityDtoList;
    }

   
    private void mockSimpleDataModel() {
        String sqlStr = "INSERT INTO plugin_packages (id, name, version) VALUES " + "  ('1', 'package_1', '1.0') "
                + ";\n" + "INSERT INTO plugin_package_data_model(id, version, package_name) VALUES "
                + "  ('1', 1, 'package_1') " + ";\n"
                + "INSERT INTO plugin_package_entities(id, data_model_id, data_model_version, package_name, name, display_name, description) VALUES "
                + "  ('1', '1', 1, 'package_1', 'entity_1', 'entity_1', 'entity_1_description') " + ";\n"
                + "INSERT INTO plugin_package_attributes(id, entity_id, reference_id, name, description, data_type) VALUES "
                + "  ('1', '1', NULL, 'attribute_1', 'attribute_1_description', 'INT') " + ";\n";
        executeSql(sqlStr);

    }

    private PluginPackageDataModelDto mockPluginPackageDataModelDtoWithIdAsAttribute(String packageName,
            String packageVersion) {

        Set<PluginPackageEntityDto> pluginPackageEntityDtos = newLinkedHashSet();
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage(packageName, packageVersion);
        pluginPackageRepository.save(package_1);

        Long now = System.currentTimeMillis();
        PluginPackageDataModelDto dataModelDto = new PluginPackageDataModelDto(null, 1, package_1.getName(), false,
                null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(), now, null);

        // mock the entityDto list with nested attribute List inside

        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setDataModelVersion(dataModelDto.getVersion());
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                if (j == 0)
                    attributeDto.setName("id");
                else
                    attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i >= 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName(packageName);
                    attributeDto.setRefEntityName("Entity_1");
                    attributeDto.setRefAttributeName("id");
                }
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributes(pluginPackageAttributeDtoList);
            pluginPackageEntityDtos.add(entityDto);
        }
        dataModelDto.setPluginPackageEntities(pluginPackageEntityDtos);

        return dataModelDto;
    }

    private PluginPackage mockPluginPackage(String name, String version) {
        PluginPackage mockPluginPackage = new PluginPackage(null, name, version, UNREGISTERED,
                new Timestamp(System.currentTimeMillis()), false, Sets.newLinkedHashSet(), Sets.newLinkedHashSet(),
                null, Sets.newLinkedHashSet(), Sets.newLinkedHashSet(), Sets.newLinkedHashSet(),
                Sets.newLinkedHashSet(), Sets.newLinkedHashSet(), Sets.newLinkedHashSet(), Sets.newLinkedHashSet());
        PluginConfig mockPlugin = new PluginConfig(null, mockPluginPackage, "mockPlugin", null, "mockEntity",
                PluginConfig.Status.DISABLED, Sets.newLinkedHashSet());
        mockPlugin.setInterfaces(Sets.newLinkedHashSet(mockPluginConfigInterface(mockPlugin)));
        mockPluginPackage.addPluginConfig(mockPlugin);

        Long now = System.currentTimeMillis();
        PluginPackageDataModel mockPluginPackageDataModel = new PluginPackageDataModel(null, 1,
                mockPluginPackage.getName(), false, null, null, PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name(),
                now, null);
        mockPluginPackage.setPluginPackageDataModel(mockPluginPackageDataModel);

        return mockPluginPackage;
    }

    private PluginConfigInterface mockPluginConfigInterface(PluginConfig pluginConfig) {
        PluginConfigInterface pluginConfigInterface = new PluginConfigInterface(null, pluginConfig, "create", "'create",
                "Qcloud_vpc_create", "/v1/qcloud/vpc/create", "POST", Sets.newLinkedHashSet(), Sets.newLinkedHashSet());
        PluginConfigInterfaceParameter inputParameter = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_INPUT, "provider_params", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        PluginConfigInterfaceParameter inputParameter2 = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_INPUT, "name", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        pluginConfigInterface
                .setInputParameters(com.google.common.collect.Sets.newHashSet(inputParameter, inputParameter2));
        PluginConfigInterfaceParameter outputParameter = new PluginConfigInterfaceParameter(null, pluginConfigInterface,
                TYPE_OUTPUT, "id", "string", MAPPING_TYPE_CMDB_CI_TYPE, null, null, "Y");
        pluginConfigInterface.setOutputParameters(com.google.common.collect.Sets.newHashSet(outputParameter));
        return pluginConfigInterface;
    }
}
