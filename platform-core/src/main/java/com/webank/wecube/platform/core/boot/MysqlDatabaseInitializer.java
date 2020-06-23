package com.webank.wecube.platform.core.boot;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlDatabaseInitializer implements DatabaseInitializer {
    
    private static final Logger log = LoggerFactory.getLogger(MysqlDatabaseInitializer.class);
    
    public static final String CREATE_SQL_NAME = "1.wecube.mysql.create.sql";
    public static final String DROP_SQL_NAME = "1.wecube.mysql.drop.sql";
    
    
    
    private String strategy;
    private DataSource dataSource;
    
    private Connection connection;
    
    @Override
    public void initialize() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getInitializeStrategy() {
        // TODO Auto-generated method stub
        return null;
    }

}
