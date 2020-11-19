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
import com.webank.wecube.platform.core.service.plugin.SystemVariableService;

public class SystemVariableServiceTest extends DatabaseBasedTest {
    @Autowired
    SystemVariableService systemVariableService;

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
    public void variableReplacementShouldSuccess() throws ScriptException {
        prepareDatabase();
        
        String newString = systemVariableService.variableReplacement("service-mgmt",
                "{{ALLOCATE_PORT}}:21000,{{BASE_MOUNT_PATH}}/service-mgmt/log:/log");
        assertThat(newString).isEqualTo(
                "20000:21000,/data/service-mgmt/log:/log");
    }
}
