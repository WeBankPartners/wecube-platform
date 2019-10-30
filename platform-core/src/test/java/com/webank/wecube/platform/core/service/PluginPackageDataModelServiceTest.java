package com.webank.wecube.platform.core.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.PluginPackageAttributeDto;
import com.webank.wecube.platform.core.dto.PluginPackageEntityDto;
import com.webank.wecube.platform.core.jpa.PluginPackageAttributeRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageEntityRepository;
import com.webank.wecube.platform.core.jpa.PluginPackageRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.webank.wecube.platform.core.jpa.PluginRepositoryIntegrationTest.mockPluginPackage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PluginPackageDataModelServiceTest extends DatabaseBasedTest {
    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    PluginPackageAttributeRepository pluginPackageAttributeRepository;
    @Autowired
    PluginPackageDataModelServiceImpl pluginPackageDataModelService;

    Integer MOCK_SIZE_PER_PACKAGE = 3;
    Integer PACKAGE_SIZE = 4;

    @Test
    public void whenRegisterDataModelShouldSuccess() {
        // register two packages with same name but different versions and their data models
        List<PluginPackageEntityDto> pluginPackageEntityDtoList = mockPluginPackageEntityDtoList("Package_1", "1.0");
        List<PluginPackageEntityDto> registeredPluginPackageEntityDtoList = pluginPackageDataModelService.register(pluginPackageEntityDtoList);
        Iterable<PluginPackageEntity> foundAllRegisteredEntityList = pluginPackageEntityRepository.findAll();
        assertThat(Iterators.size(foundAllRegisteredEntityList.iterator())).isEqualTo(registeredPluginPackageEntityDtoList.size());

        List<PluginPackageEntityDto> pluginPackageEntityDtoList2 = mockPluginPackageEntityDtoList("Package_1", "2.0");
        List<PluginPackageEntityDto> registeredPluginPackageEntityDtoList2 = pluginPackageDataModelService.register(pluginPackageEntityDtoList2);
        Iterable<PluginPackageEntity> foundAllRegisteredEntityList2 = pluginPackageEntityRepository.findAll();
        assertThat(Iterators.size(foundAllRegisteredEntityList2.iterator())).isEqualTo(MOCK_SIZE_PER_PACKAGE * 2);

        // data model with false info
        List<PluginPackageEntityDto> pluginPackageEntityDtoList3 = mockFalsePluginPackageEntityDtoList("Package_2", "2.0", "1.0");
        try {
            pluginPackageDataModelService.register(pluginPackageEntityDtoList3);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage().contains("Cannot found the specified plugin model entity with package name:")).isEqualTo(true);
        }

        // data model reference to a registered package with one attribute refer to package1`entity_1
        pluginPackageRepository.save(mockPluginPackage("Package_3", "1.0"));
        List<PluginPackageEntityDto> pluginPackageEntityDtoList4 = mockPluginPackageEntityDtoListByRegisteredPackage("Package_3", "1.0");
        List<PluginPackageEntityDto> registeredDtoList4 = pluginPackageDataModelService.register(pluginPackageEntityDtoList4);
        Iterable<PluginPackageEntity> all = pluginPackageEntityRepository.findAll();
        assertThat(registeredDtoList4.size()).isEqualTo(pluginPackageEntityDtoList4.size());

        // data model reference to a registered package with no attribute reference
        pluginPackageRepository.save(mockPluginPackage("Package_4", "1.0"));
        List<PluginPackageEntityDto> pluginPackageEntityDtoList5 = mockPluginPackageEntityDtoListWithNoAttrRef("Package_4", "1.0");
        List<PluginPackageEntityDto> registeredDtoList5 = pluginPackageDataModelService.register(pluginPackageEntityDtoList5);
        assertThat(registeredDtoList5.size()).isEqualTo(pluginPackageEntityDtoList5.size());

        // test current package number and data model size
        Iterable<PluginPackage> allPackages = pluginPackageRepository.findAll();
        assertThat(Iterables.size(allPackages)).isEqualTo(PACKAGE_SIZE + 1);  // because in mock false function, the package is successfully saved
        Iterable<PluginPackageEntity> allDataModels = pluginPackageEntityRepository.findAll();
        assertThat(Iterables.size(allDataModels)).isEqualTo(PACKAGE_SIZE * MOCK_SIZE_PER_PACKAGE);
    }

    @Test
    public void whenRegisterSamePackageShouldSuccess() {
        List<PluginPackageEntityDto> pluginPackageEntityDtoList = mockPluginPackageEntityDtoList("Package_1", "1.0");
        pluginPackageDataModelService.register(pluginPackageEntityDtoList);
        pluginPackageDataModelService.register(pluginPackageEntityDtoList);

        Iterable<PluginPackageEntity> allEntities = pluginPackageEntityRepository.findAll();
        assertThat(Iterables.size(allEntities)).isEqualTo(MOCK_SIZE_PER_PACKAGE * 2);

        Optional<List<PluginPackageEntity>> foundLatestByPackageNameOpt = pluginPackageEntityRepository.findAllLatestEntityByPluginPackage_name("Package_1");
        if (!foundLatestByPackageNameOpt.isPresent()) {
            fail("Cannot find entities");
        }
        List<PluginPackageEntity> pluginPackageEntities = foundLatestByPackageNameOpt.get();
        assertThat(pluginPackageEntities.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE);

        System.out.println(foundLatestByPackageNameOpt);
    }

    @Test
    public void whenOverviewShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        List<PluginPackageEntityDto> registeredAllDataModelList = pluginPackageDataModelService.overview();
        assertThat(registeredAllDataModelList.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE * (PACKAGE_SIZE - 1));  // because the package 2 hasn't been registered to database
        registeredAllDataModelList.forEach(registeredDataModel -> assertThat(registeredDataModel.getAttributeDtoList().size()).isEqualTo(3));
    }


//    @Test
//    public void whenPackageViewByPackageNameAndVersionShouldSuccess() {
//        whenRegisterDataModelShouldSuccess();
//        try {
//            pluginPackageDataModelService.packageView("falsePackageName", "falsePackageVersion");
//        } catch (WecubeCoreException ex) {
//            assertThat(ex.getMessage()).contains("Cannot find data model");
//        }
//
//        List<PluginPackageEntityDto> foundEntityDtoListByPackageNameAndVersion = pluginPackageDataModelService.packageView("Package_1", "1.0");
//        assertThat(foundEntityDtoListByPackageNameAndVersion.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE);
//        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageName()).isEqualTo("Package_1");
//        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageVersion()).isEqualTo("1.0");
//    }

    @Test
    public void whenPackageViewByPackageIdShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        List<PluginPackageEntityDto> foundEntityDtoListByPackageNameAndVersion = pluginPackageDataModelService.packageView(1);
        assertThat(foundEntityDtoListByPackageNameAndVersion.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE);
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageName()).isEqualTo("Package_1");
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageVersion()).isEqualTo("1.0");
    }

//    @Test
//    public void whenDeleteByPackageNameAndVersionShouldSuccess() {
//        whenRegisterDataModelShouldSuccess();
//        Iterable<PluginPackageEntity> allAfterDelete = pluginPackageEntityRepository.findAll();
//        List<PluginPackageEntityDto> overview = pluginPackageDataModelService.overview();
//        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(MOCK_SIZE_PER_PACKAGE * PACKAGE_SIZE);
//        try {
//            pluginPackageDataModelService.deleteModel("Package_1", "1.0");
//        } catch (WecubeCoreException ex) {
//            assertThat(ex.getMessage().contains("is still referenced by others, delete operation will terminate.")).isEqualTo(true);
//        }
//        // because no one refer to data model with package name: Package_1 and version: 2.0 so that this model can be deleted
//        pluginPackageDataModelService.deleteModel("Package_1", "2.0");
//        allAfterDelete = pluginPackageEntityRepository.findAll();
//        overview = pluginPackageDataModelService.overview();
//        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(MOCK_SIZE_PER_PACKAGE * (PACKAGE_SIZE - 1));
//    }

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
            entityDto.setPackageVersion(packageVersion);
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
            entityDto.setAttributeDtoList(pluginPackageAttributeDtoList);
            pluginPackageEntityDtoList.add(entityDto);
        }

        return pluginPackageEntityDtoList;
    }

    private List<PluginPackageEntityDto> mockPluginPackageEntityDtoListByRegisteredPackage(String packageName, String packageVersion) {

        List<PluginPackageEntityDto> pluginPackageEntityDtoList = new ArrayList<>();

        // mock the entityDto list with nested attribute List inside
        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setPackageVersion(packageVersion);
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
            entityDto.setAttributeDtoList(pluginPackageAttributeDtoList);
            pluginPackageEntityDtoList.add(entityDto);
        }

        return pluginPackageEntityDtoList;
    }

    private List<PluginPackageEntityDto> mockPluginPackageEntityDtoListWithNoAttrRef(String packageName, String packageVersion) {

        List<PluginPackageEntityDto> pluginPackageEntityDtoList = new ArrayList<>();

        // mock the entityDto list with nested attribute List inside
        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setPackageVersion(packageVersion);
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginPackageAttributeDto> pluginPackageAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginPackageAttributeDto attributeDto = new PluginPackageAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                pluginPackageAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributeDtoList(pluginPackageAttributeDtoList);
            pluginPackageEntityDtoList.add(entityDto);
        }

        return pluginPackageEntityDtoList;
    }

    private List<PluginPackageEntityDto> mockFalsePluginPackageEntityDtoList(String packageName, String packageVersion, String referenceVersion) {

        List<PluginPackageEntityDto> pluginPackageEntityDtoList = new ArrayList<>();
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage(packageName, packageVersion);
        pluginPackageRepository.save(package_1);
        // mock the entityDto list with nested attribute List inside

        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginPackageEntityDto entityDto = new PluginPackageEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setPackageVersion(packageVersion);
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
            entityDto.setAttributeDtoList(pluginPackageAttributeDtoList);
            pluginPackageEntityDtoList.add(entityDto);
        }

        return pluginPackageEntityDtoList;
    }
}
