package com.webank.wecube.platform.core.config;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {
    private static Logger logger = LoggerFactory.getLogger(TestConfig.class);

    @Bean(name = "h2server", destroyMethod = "stop")
    public Server getH2Server() {
        Server h2Server;
        try {
            h2Server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082");
            h2Server.start();
            return h2Server;
        } catch (SQLException e) {
            logger.info("Fail to start H2 server.", e);
        }
        return null;
    }

}
