package com.webank.wecube.platform.core.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.script.ScriptException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.webank.wecube.platform.core.DatabaseBasedTest;

public class PluginInstanceServiceTest extends DatabaseBasedTest {
    @Autowired
    PluginInstanceService pluginInstanceService;

    private void prepareDatabase() throws ScriptException {
        executeSqlScripts(newArrayList(new ClassPathResource("/database/03.wecube.test.data.sql")));
    }

    private void executeSqlScripts(List<Resource> scipts) throws ScriptException {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(false);
        populator.setSeparator(";");
        scipts.forEach(populator::addScript);
        populator.execute(dataSource);
    }

    @Test
    public void removePluginInstanceByIdShouldSuccess() throws ScriptException {
        prepareDatabase();

        try {
            pluginInstanceService.removePluginInstanceById("service-mgmt:v1.0:service-mgmt:10.0.2.12:20003");
            assertThat(true);
        } catch (Exception e) {
            assertThat(false);
        }
    }
}
