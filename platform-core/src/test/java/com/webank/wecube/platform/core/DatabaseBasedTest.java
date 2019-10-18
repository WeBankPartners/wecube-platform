package com.webank.wecube.platform.core;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Transactional
public abstract class DatabaseBasedTest extends BaseSpringBootTest {
    @Autowired
    protected DataSource dataSource;

    @Before
    public void setup() {
        cleanUpDatabase();
        prepareDatabase();
    }

    @After
    public void cleanUp() {
        cleanUpDatabase();
    }

    private void prepareDatabase() {
        executeSqlScripts(newArrayList(
                new ClassPathResource("/database/01.wecube.schema.sql")
        ));
    }

    private void cleanUpDatabase() {
        executeSqlScript("/database/00.drop.all.sql");
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
