package com.webank.wecube.platform.auth.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

/**
 * 
 * @author gavin
 *
 */
@SpringBootApplication
@EnableEncryptableProperties
public class PlatformAuthServerApplication {
    public static final String ENV_KEY_LOAD_DEFAULT_PROPERTIES = "loadDefaultProperties";
    private static final Logger log = LoggerFactory.getLogger(PlatformAuthServerApplication.class);

    public static void main(String[] args) {
        SpringApplicationBuilder b = new SpringApplicationBuilder(PlatformAuthServerApplication.class);
        String loadDefaultProperties = System.getenv(ENV_KEY_LOAD_DEFAULT_PROPERTIES);

        if (loadDefaultProperties != null) {
            Properties loadedProperties = loadDefaultProperties();
            b.properties(loadedProperties);
            if (log.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("loaded default properties:\n");
                
                loadedProperties.forEach( (k , v) -> {
                    sb.append("key=").append(k).append(",value=").append(v).append("\n");
                });
                
                log.info(sb.toString());
            }
        }
        ConfigurableApplicationContext ctx = b.run(args);

        log.info("{} started for : {} ", PlatformAuthServerApplication.class.getSimpleName(),
                ctx.getEnvironment().getActiveProfiles());
    }

    private static Properties loadDefaultProperties() {
        Properties defaultProperties = new Properties();
        String filename = "platform-auth-server.properties";
        File propertiesFile = new File(System.getProperty("user.home") + File.separator + filename);
        if (!propertiesFile.exists()) {
            return new Properties();
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(propertiesFile);
            defaultProperties.load(fis);
        } catch (Exception e) {
            return new Properties();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }

        return defaultProperties;
    }
}
