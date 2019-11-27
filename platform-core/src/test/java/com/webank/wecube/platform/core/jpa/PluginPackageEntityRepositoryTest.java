package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginPackageEntityRepositoryTest extends DatabaseBasedTest {
    @Autowired
    private PluginPackageEntityRepository entityRepository;
    @Autowired
    private PluginPackageDataModelRepository dataModelRepository;

    @Test
    public void givenDescriptionIsNullForEntityWhenSaveThenShouldSucceed() {

        PluginPackageDataModel dataModel = new PluginPackageDataModel();
        dataModel.setPackageName("wecmdb");
        dataModel.setVersion(1);
        dataModel.setUpdateSource(PluginPackageDataModelDto.Source.PLUGIN_PACKAGE.name());
        dataModel.setDynamic(true);
        dataModel.setUpdatePath("/data-model");
        dataModel.setUpdateMethod("GET");
        dataModel.setUpdateTime(System.currentTimeMillis());

        String system_design = "system_design";
        PluginPackageEntity entity = new PluginPackageEntity(dataModel, system_design, system_design, null);

        dataModel.setPluginPackageEntities(newHashSet(entity));

        dataModelRepository.save(dataModel);

        Iterable<PluginPackageEntity> entities = entityRepository.findAll();
        assertThat(entities).isNotNull();
        Iterator<PluginPackageEntity> iterator = entities.iterator();
        assertThat(iterator).isNotNull();
        PluginPackageEntity packageEntity = iterator.next();
        assertThat(packageEntity).isNotNull();
        assertThat(packageEntity.getName()).isEqualTo(system_design);
    }
}