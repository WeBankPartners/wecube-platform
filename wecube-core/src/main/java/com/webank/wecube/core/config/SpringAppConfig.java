package com.webank.wecube.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.webank.wecube.core.commons.ApplicationProperties;
import com.webank.wecube.core.commons.ApplicationProperties.ApiProxyProperties;
import com.webank.wecube.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.core.commons.ApplicationProperties.HttpClientProperties;
import com.webank.wecube.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.core.commons.ApplicationProperties.S3Properties;

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
public class SpringAppConfig {

}
