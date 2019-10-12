package com.webank.wecube.core.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.webank.wecube.core.DatabaseBasedTest;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.domain.plugin.PluginModelEntity;
import com.webank.wecube.core.domain.plugin.PluginPackage;
import com.webank.wecube.core.dto.PluginModelAttributeDto;
import com.webank.wecube.core.dto.PluginModelEntityDto;
import com.webank.wecube.core.jpa.PluginModelAttributeRepository;
import com.webank.wecube.core.jpa.PluginModelEntityRepository;
import com.webank.wecube.core.jpa.PluginPackageRepository;
import com.webank.wecube.core.service.plugin.PluginModelServiceImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static com.webank.wecube.core.jpa.PluginRepositoryIntegrationTest.mockPluginPackage;

public class PluginModelServiceTest extends DatabaseBasedTest {
    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginModelEntityRepository pluginModelEntityRepository;
    @Autowired
    PluginModelAttributeRepository pluginModelAttributeRepository;
    @Autowired
    PluginModelServiceImpl pluginModelService;

    Integer MOCK_SIZE_PER_PACKAGE = 3;
    Integer PACKAGE_SIZE = 3;

    @Test
    public void whenRegisterDataModelShouldSuccess() {
        // register two packages with same name but different versions and their data models
        List<PluginModelEntityDto> pluginModelEntityDtoList = mockPluginModelEntityDtoList("Package_1", "1.0", "1.0");
        List<PluginModelEntityDto> pluginModelEntityDtoList2 = mockPluginModelEntityDtoList("Package_1", "2.0", "1.0");
        pluginModelEntityDtoList.addAll(pluginModelEntityDtoList2);
        List<PluginModelEntityDto> registeredPluginModelEntityDtoList = pluginModelService.register(pluginModelEntityDtoList);
        Iterable<PluginModelEntity> foundAllRegisteredEntityList = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(foundAllRegisteredEntityList.iterator())).isEqualTo(registeredPluginModelEntityDtoList.size());

        // data model with false info
        List<PluginModelEntityDto> pluginModelEntityDtoList3 = mockFalsePluginModelEntityDtoList("Package_2", "2.0", "1.0");
        try {
            pluginModelService.register(pluginModelEntityDtoList3);
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage().contains("Cannot found the specified plugin model entity with package name:")).isEqualTo(true);
        }

        // data model reference to a registered package
        pluginPackageRepository.save(mockPluginPackage("Package_3", "1.0"));
        List<PluginModelEntityDto> pluginModelEntityDtoList4 = mockPluginModelEntityDtoListByRegisteredPackage("Package_3", "1.0", "1.0");
        List<PluginModelEntityDto> registeredDtoList4 = pluginModelService.register(pluginModelEntityDtoList4);
        assertThat(registeredDtoList4.size()).isEqualTo(pluginModelEntityDtoList4.size());

        // test current package number and data model size
        Iterable<PluginPackage> allPackages = pluginPackageRepository.findAll();
        assertThat(Iterables.size(allPackages)).isEqualTo(PACKAGE_SIZE + 1);  // because in mock false function, the package is successfully saved
        Iterable<PluginModelEntity> allDataModels = pluginModelEntityRepository.findAll();
        assertThat(Iterables.size(allDataModels)).isEqualTo(PACKAGE_SIZE * MOCK_SIZE_PER_PACKAGE);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void whenRegisterSamePackageShouldFail() {
        whenRegisterDataModelShouldSuccess();
        // register same package should fail
        List<PluginModelEntityDto> pluginModelEntityDtoList = mockPluginModelEntityDtoList("Package_1", "1.0", "1.0");
        pluginModelService.register(pluginModelEntityDtoList);
    }

    @Test
    public void whenOverviewShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        List<PluginModelEntityDto> registeredAllDataModelList = pluginModelService.overview();
        assertThat(registeredAllDataModelList.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE * PACKAGE_SIZE);
        registeredAllDataModelList.forEach(registeredDataModel -> assertThat(registeredDataModel.getAttributeDtoList().size()).isEqualTo(3));
    }


    @Test
    public void whenPackageViewByPackageNameAndVersionShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        try {
            pluginModelService.packageView("falsePackageName", "falsePackageVersion");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage()).contains("Cannot find datamodel");
        }

        List<PluginModelEntityDto> foundEntityDtoListByPackageNameAndVersion = pluginModelService.packageView("Package_1", "1.0");
        assertThat(foundEntityDtoListByPackageNameAndVersion.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE);
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageName()).isEqualTo("Package_1");
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageVersion()).isEqualTo("1.0");
    }

    @Test
    public void whenPackageViewByPackageIdShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        List<PluginModelEntityDto> foundEntityDtoListByPackageNameAndVersion = pluginModelService.packageView(1);
        assertThat(foundEntityDtoListByPackageNameAndVersion.size()).isEqualTo(MOCK_SIZE_PER_PACKAGE);
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageName()).isEqualTo("Package_1");
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageVersion()).isEqualTo("1.0");
    }

    @Test
    public void whenDeleteByPackageNameAndVersionShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        Iterable<PluginModelEntity> allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(MOCK_SIZE_PER_PACKAGE * PACKAGE_SIZE);
        try {
            pluginModelService.deleteModel("Package_1", "1.0");
        } catch (WecubeCoreException ex) {
            assertThat(ex.getMessage().contains("is still referenced by others, delete operation will terminate.")).isEqualTo(true);
        }
        pluginModelService.deleteModel("Package_1", "2.0");
        allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(MOCK_SIZE_PER_PACKAGE * (PACKAGE_SIZE - 1));
    }

    private List<PluginModelEntityDto> mockPluginModelEntityDtoList(String packageName, String packageVersion, String referenceVersion) {

        List<PluginModelEntityDto> pluginModelEntityDtoList = new ArrayList<>();
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage(packageName, packageVersion);
        pluginPackageRepository.save(package_1);

        // mock the entityDto list with nested attribute List inside

        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginModelEntityDto entityDto = new PluginModelEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setPackageVersion(packageVersion);
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginModelAttributeDto> pluginModelAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginModelAttributeDto attributeDto = new PluginModelAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i >= 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName(packageName);
                    attributeDto.setRefEntityName("Entity_1");
                    attributeDto.setRefAttributeName("Attribute_1");
                    attributeDto.setRefPackageVersion(referenceVersion);
                }
                pluginModelAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributeDtoList(pluginModelAttributeDtoList);
            pluginModelEntityDtoList.add(entityDto);
        }

        return pluginModelEntityDtoList;
    }

    private List<PluginModelEntityDto> mockPluginModelEntityDtoListByRegisteredPackage(String packageName, String packageVersion, String referenceVersion) {

        List<PluginModelEntityDto> pluginModelEntityDtoList = new ArrayList<>();

        // mock the entityDto list with nested attribute List inside
        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginModelEntityDto entityDto = new PluginModelEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setPackageVersion(packageVersion);
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginModelAttributeDto> pluginModelAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginModelAttributeDto attributeDto = new PluginModelAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i > 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName("Package_1");
                    attributeDto.setRefEntityName("Entity_1");
                    attributeDto.setRefAttributeName("Attribute_1");
                    attributeDto.setRefPackageVersion(referenceVersion);
                }
                pluginModelAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributeDtoList(pluginModelAttributeDtoList);
            pluginModelEntityDtoList.add(entityDto);
        }

        return pluginModelEntityDtoList;
    }

    private List<PluginModelEntityDto> mockFalsePluginModelEntityDtoList(String packageName, String packageVersion, String referenceVersion) {

        List<PluginModelEntityDto> pluginModelEntityDtoList = new ArrayList<>();
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage(packageName, packageVersion);
        pluginPackageRepository.save(package_1);
        // mock the entityDto list with nested attribute List inside

        for (int i = 0; i < MOCK_SIZE_PER_PACKAGE; i++) {
            PluginModelEntityDto entityDto = new PluginModelEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName(packageName);
            entityDto.setPackageVersion(packageVersion);
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginModelAttributeDto> pluginModelAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MOCK_SIZE_PER_PACKAGE; j++) {
                PluginModelAttributeDto attributeDto = new PluginModelAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("str");
                if (i > 1 && j >= 1) {
                    attributeDto.setDataType("ref");
                    attributeDto.setRefPackageName("faultPackageName");
                    attributeDto.setRefEntityName("faultEntityName");
                    attributeDto.setRefAttributeName("faultAttributeName");
                    attributeDto.setRefPackageVersion(referenceVersion);
                }
                pluginModelAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributeDtoList(pluginModelAttributeDtoList);
            pluginModelEntityDtoList.add(entityDto);
        }

        return pluginModelEntityDtoList;
    }
}
