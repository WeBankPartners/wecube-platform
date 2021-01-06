package com.webank.wecube.platform.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.webank.wecube.platform.core.config.SpringAppConfig;
import com.webank.wecube.platform.core.config.SpringWebConfig;

@SpringBootApplication
public class Application extends AbstractAnnotationConfigDispatcherServletInitializer {

    public static void main(String[] args) {
        System.setProperty("hibernate.dialect.storage_engine", "innodb");
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { SpringAppConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { SpringWebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

}
