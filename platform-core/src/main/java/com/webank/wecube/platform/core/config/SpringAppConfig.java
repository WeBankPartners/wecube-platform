package com.webank.wecube.platform.core.config;

import com.webank.wecube.platform.core.parser.PluginPackageValidator;
import com.webank.wecube.platform.core.support.RealS3Client;
import com.webank.wecube.platform.core.support.S3Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ApiProxyProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.HttpClientProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.S3Properties;
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
@ComponentScan({ "com.webank.wecube.platform.core.service" })
@EntityScan(basePackages = { "com.webank.wecube.platform.core" })
@EnableJpaRepositories(basePackages = { "com.webank.wecube.platform.core" })
@EnablePlatformWorkflowApplication
public class SpringAppConfig {

    @Autowired
    private S3Properties s3Properties;

    @Bean
    public S3Client configS3Client() {
        return new RealS3Client(s3Properties.getEndpoint(), s3Properties.getAccessKey(), s3Properties.getSecretKey());
    }

    @Bean
    public PluginPackageValidator configValidator() {
        return new PluginPackageValidator();
    }
}
