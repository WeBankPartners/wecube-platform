package com.webank.wecube.platform.auth.server.boot;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.auth.server.config.AuthServerProperties;

@Component("applicationBootstrap")
public class ApplicationBootstrap {
    private static final Logger log = LoggerFactory.getLogger(ApplicationBootstrap.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthServerProperties authServerProperties;

    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ApplicationVersionInfo applicationVersionInfo;

    @PostConstruct
    public void afterPropertiesSetting() throws Exception {
        try {
            databaseInitializer = new MysqlDatabaseInitializer(authServerProperties.getDbInitStrategy(), dataSource,
                    applicationVersionInfo);
            bootstrap();
        } catch (Exception e) {
            log.error("", e);
            throw e;
        }
    }

    private void bootstrap() {
        initDatabaseSchema();
    }

    private void initDatabaseSchema() {
        databaseInitializer.initialize();
    }
}
