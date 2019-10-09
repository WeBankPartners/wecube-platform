package com.webank.wecube.core.service;

import com.google.common.collect.Iterators;
import com.webank.wecube.core.DatabaseBasedTest;
import com.webank.wecube.core.domain.plugin.PluginModelAttribute;
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

    Integer MAX_MOCK_SIZE = 3;

    @Test
    public void whenRegisterDataModelShouldSuccess() {
        List<PluginModelEntityDto> pluginModelEntityDtoList = mockPluginModelEntityDtoList();
        List<PluginModelEntityDto> registeredPluginModelEntityDtoList = pluginModelService.register(pluginModelEntityDtoList);
        Iterable<PluginModelEntity> foundAllRegisteredEntityList = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(foundAllRegisteredEntityList.iterator())).isEqualTo(registeredPluginModelEntityDtoList.size());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void whenRegisterSamePackageShouldFail() {
        whenRegisterDataModelShouldSuccess();
        List<PluginModelEntityDto> pluginModelEntityDtoList = mockPluginModelEntityDtoList();
        pluginModelService.register(pluginModelEntityDtoList);
    }

    @Test
    public void whenOverviewShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        List<PluginModelEntityDto> registeredAllDataModelList = pluginModelService.overview();
        assertThat(registeredAllDataModelList.size()).isEqualTo(3);
        registeredAllDataModelList.forEach(registeredDataModel -> assertThat(registeredDataModel.getAttributeDtoList().size()).isEqualTo(3));
    }


    @Test
    public void whenPackageViewByPackageNameAndVersionShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        List<PluginModelEntityDto> foundEntityDtoListByPackageNameAndVersion = pluginModelService.packageView("Package_1", "1.0");
        assertThat(foundEntityDtoListByPackageNameAndVersion.size()).isEqualTo(3);
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageName()).isEqualTo("Package_1");
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageVersion()).isEqualTo("1.0");
    }

    @Test
    public void whenPackageViewByPackageIdShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        List<PluginModelEntityDto> foundEntityDtoListByPackageNameAndVersion = pluginModelService.packageView(1);
        assertThat(foundEntityDtoListByPackageNameAndVersion.size()).isEqualTo(3);
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageName()).isEqualTo("Package_1");
        assertThat(foundEntityDtoListByPackageNameAndVersion.get(0).getPackageVersion()).isEqualTo("1.0");
    }

    @Test
    public void whenDeleteByPackageNameShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        Iterable<PluginModelEntity> allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(3);
        pluginModelService.deleteEntity("Package_1");
        allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(0);
    }

    // TODO
    @Test
    public void whenDeleteEntityByPackageNameAndAllEntityNamesShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        Iterable<PluginModelEntity> allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(3);
        pluginModelService.deleteEntity("Package_1", "Entity_1", "Entity_2", "Entity_3");
        allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(0);
    }

    // TODO
    @Test
    public void whenDeleteEntityByPackageNameAndEntityNameShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        Iterable<PluginModelEntity> allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(3);
        pluginModelService.deleteEntity("Package_1", "Entity_1");
        allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(2);

        pluginModelService.deleteEntity("Package_1", "Entity_2");
        allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(1);

        pluginModelService.deleteEntity("Package_1", "Entity_3");
        allAfterDelete = pluginModelEntityRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(0);
    }

    // TODO
    @Test
    public void whenDeleteAttributeByEntityNameShouldSuccess() {
        whenRegisterDataModelShouldSuccess();
        Iterable<PluginModelAttribute> allAfterDelete = pluginModelAttributeRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(MAX_MOCK_SIZE * MAX_MOCK_SIZE);
        pluginModelService.deleteAttribute("Entity_1");
        allAfterDelete = pluginModelAttributeRepository.findAll();
        assertThat(Iterators.size(allAfterDelete.iterator())).isEqualTo(MAX_MOCK_SIZE * (MAX_MOCK_SIZE - 1));
    }

    private List<PluginModelEntityDto> mockPluginModelEntityDtoList() {
        // mock a registered plugin first then save
        PluginPackage package_1 = mockPluginPackage("Package_1", "1.0");
        PluginPackage savedPackage_1 = pluginPackageRepository.save(package_1);
        // mock the entityDto list with nested attribute List inside

        List<PluginModelEntityDto> pluginModelEntityDtoList = new ArrayList<>();
        for (int i = 0; i < MAX_MOCK_SIZE; i++) {
            PluginModelEntityDto entityDto = new PluginModelEntityDto();
            entityDto.setName(String.format("Entity_%d", i + 1));
            entityDto.setPackageName("Package_1");
            entityDto.setPackageVersion("1.0");
            entityDto.setDescription(String.format("Entity_%d_description", i + 1));
            List<PluginModelAttributeDto> pluginModelAttributeDtoList = new ArrayList<>();
            for (int j = 0; j < MAX_MOCK_SIZE; j++) {
                PluginModelAttributeDto attributeDto = new PluginModelAttributeDto();
                attributeDto.setDescription(String.format("Attribute_%d", j));
                attributeDto.setName(String.format("Attribute_%d", j));
                attributeDto.setDataType("String");
                if (i > 1 && j >= 1) {
                    attributeDto.setRefPackageName("Package_1");
                    attributeDto.setRefEntityName("Entity_1");
                    attributeDto.setRefAttributeName("Attribute_1");
                    attributeDto.setDataType("ref");
                }
                pluginModelAttributeDtoList.add(attributeDto);
            }
            entityDto.setAttributeDtoList(pluginModelAttributeDtoList);
            pluginModelEntityDtoList.add(entityDto);
        }

        return pluginModelEntityDtoList;
    }

    private List<PluginModelEntityDto> mockUpdatePluginModelEntityDtoList() {
        // TODO: finish mock update plugin model entity dto list
        List<PluginModelEntityDto> pluginModelEntityDtoList = new ArrayList<>();

        return null;
    }
}
