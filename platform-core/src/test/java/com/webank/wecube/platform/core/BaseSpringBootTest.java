package com.webank.wecube.platform.core;

import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ApiProxyProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.HttpClientProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.PluginProperties;
import com.webank.wecube.platform.core.commons.ApplicationProperties.S3Properties;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@EnableConfigurationProperties({
    ApplicationProperties.class, 
    HttpClientProperties.class, 
    CmdbDataProperties.class, 
    PluginProperties.class, 
    S3Properties.class,
    ApiProxyProperties.class
})
public abstract class BaseSpringBootTest {
}
