package com.webank.wecube.platform.core.jpa;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.BaseSpringBootTest;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.dto.PluginPackageDataModelDto;
import com.webank.wecube.platform.core.utils.constant.DataModelDataType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginPackageAttributeRepositoryTest extends BaseSpringBootTest {
    @Autowired
    private PluginPackageAttributeRepository attributeRepository;
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
        PluginPackageEntity entity = new PluginPackageEntity(dataModel, system_design, system_design, "System Design");

        String attributeDescription = null;
        PluginPackageAttribute attribute = new PluginPackageAttribute(entity, null, "id", attributeDescription, DataModelDataType.Integer.getCode());

        entity.setPluginPackageAttributeList(newArrayList(attribute));
        dataModel.setPluginPackageEntities(newHashSet(entity));

        dataModelRepository.save(dataModel);

        Iterable<PluginPackageAttribute> attributes = attributeRepository.findAll();
        assertThat(attributes).isNotNull();
        Iterator<PluginPackageAttribute> iterator = attributes.iterator();
        assertThat(iterator).isNotNull();
        PluginPackageAttribute pluginPackageAttribute = iterator.next();
        assertThat(pluginPackageAttribute).isNotNull();
        assertThat(pluginPackageAttribute.getName()).isEqualTo("id");
        assertThat(pluginPackageAttribute.getDataType()).isEqualTo(DataModelDataType.Integer.getCode());
    }
}