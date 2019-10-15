package com.webank.wecube.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.webank.wecube.core.commons.ApplicationProperties;
import com.webank.wecube.core.commons.ApplicationProperties.ApiProxyProperties;
import com.webank.wecube.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.core.commons.ApplicationProperties.HttpClientProperties;
import com.webank.wecube.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.core.commons.ApplicationProperties.S3Properties;
import com.webank.wecube.platform.workflow.EnablePlatformWorkflowApplication;

@Configuration
@EnableConfigurationProperties({
        ApplicationProperties.class,
        HttpClientProperties.class,
        CmdbDataProperties.class,
        PluginProperties.class,
        S3Properties.class,
        ApiProxyProperties.class,
        ResourceProperties.class
})
@ComponentScan({ "com.webank.wecube.core.service" })
@EntityScan(basePackages = { "com.webank.wecube.core" })
@EnableJpaRepositories(basePackages = { "com.webank.wecube.core" })
@EnablePlatformWorkflowApplication
public class SpringAppConfig {

}
