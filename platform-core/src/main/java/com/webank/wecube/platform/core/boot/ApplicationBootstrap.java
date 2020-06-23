package com.webank.wecube.platform.core.boot;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("applicationBootstrap")
public class ApplicationBootstrap {

    @Autowired
    private DataSource dataSource;
}
