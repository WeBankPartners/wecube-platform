package com.webank.wecube.core.config;

import com.webank.wecube.core.commons.ApplicationProperties;
import com.webank.wecube.core.commons.ApplicationProperties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ApplicationProperties.class, HttpClientProperties.class, CmdbDataProperties.class, PluginProperties.class, S3Properties.class, ResourceProperties.class})
@ComponentScan({"com.webank.wecube.core.service"})
public class SpringAppConfig {

}

