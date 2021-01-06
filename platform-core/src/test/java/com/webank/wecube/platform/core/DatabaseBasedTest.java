package com.webank.wecube.platform.core;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Transactional
public abstract class DatabaseBasedTest extends BaseSpringBootTest {
    @Autowired
    protected DataSource dataSource;
    
    @Autowired 
    private ProcessEngine processEngin;

    @Before
    public void setup() throws Exception {
        cleanUpDatabase();
        refreshProcessSchema();
        prepareDatabase();
    }

    @After
    public void cleanUp() {
    }

    private void prepareDatabase() {
        executeSqlScripts(newArrayList(
                new ClassPathResource("/database/01.wecube.schema.sql")
//                new ClassPathResource("/database/02.wecube.system.data.sql")
        ));
    }
    
    private void refreshProcessSchema() throws Exception {
        if(processEngin instanceof ProcessEngineImpl) {
            Method executeSchemaMethod = ProcessEngineImpl.class.getDeclaredMethod("executeSchemaOperations");
            executeSchemaMethod.setAccessible(true);
            executeSchemaMethod.invoke(processEngin);
        }
    }

    private void cleanUpDatabase() {
//        executeSqlScript("/database/00.drop.all.sql");
    }

    protected void executeSql(String sql) {
        executeSqlScripts(newArrayList(new ByteArrayResource(sql.getBytes())));
    }

    protected void executeSqlScript(String sqlScript) {
        executeSqlScripts(newArrayList(new ClassPathResource(sqlScript)));
    }

    private void executeSqlScripts(List<Resource> scipts) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.setIgnoreFailedDrops(false);
        populator.setSeparator(";");
        scipts.forEach(populator::addScript);
        populator.execute(dataSource);
    }
}
