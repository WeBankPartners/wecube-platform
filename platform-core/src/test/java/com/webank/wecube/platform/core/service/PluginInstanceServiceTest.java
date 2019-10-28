package com.webank.wecube.platform.core.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.plugin.PluginMysqlInstance;
import com.webank.wecube.platform.core.domain.plugin.PluginPackage;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageRuntimeResourcesMysql;
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

import static com.webank.wecube.platform.core.jpa.PluginRepositoryIntegrationTest.mockPluginPackage;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginInstanceServiceTest extends DatabaseBasedTest {
    @Autowired
    PluginPackageRepository pluginPackageRepository;
    @Autowired
    PluginPackageEntityRepository pluginPackageEntityRepository;
    @Autowired
    PluginPackageAttributeRepository pluginPackageAttributeRepository;
    @Autowired
    PluginPackageDataModelServiceImpl pluginPackageDataModelService;
    @Autowired
    PluginInstanceService pluginInstanceService;

    @Test
    public void whenCreatePluginMysqlDatabaseShouldSuccess() {

        PluginPackageRuntimeResourcesMysql mysqlInfo = new PluginPackageRuntimeResourcesMysql();
        PluginMysqlInstance expectedResult = new PluginMysqlInstance();

        PluginMysqlInstance actualResult = pluginInstanceService.createPluginMysqlDatabase(mysqlInfo);

        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void whenCreatePluginS3BucketSuccess() {
    }

    @Test
    public void whenCreatePluginDockerInstanceShouldSuccess() {

    }

}
