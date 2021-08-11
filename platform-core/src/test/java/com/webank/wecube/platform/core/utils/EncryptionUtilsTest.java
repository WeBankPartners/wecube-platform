package com.webank.wecube.platform.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;

public class EncryptionUtilsTest extends DatabaseBasedTest {
    private static final Logger log = LoggerFactory.getLogger(EncryptionUtilsTest.class);

    @Autowired
    private ResourceProperties resourceProperties;
    
    @Test
    public void encryptPasswordTest() {
        String password = "qq123456";
        String additionalSalt = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String encryptedPassword = EncryptionUtils.encryptWithAesCbc(password,
                additionalSalt);
        
        assertThat(encryptedPassword).isNotNull();

        String decryptedPassword = EncryptionUtils.decryptWithAesCbc(encryptedPassword, additionalSalt);
        assertThat(decryptedPassword).isEqualTo(password);
    }

    @Test
    public void encryptPasswordTestS3Host() {
        ***REMOVED***;
        String additionalSalt = "s3Host";
        String encryptedPassword = EncryptionUtils.encryptWithAes(password,
                resourceProperties.getPasswordEncryptionSeed(), additionalSalt);
        log.info("encryptedPassword: " + encryptedPassword);

        assertThat(EncryptionUtils.decryptWithAes(encryptedPassword, resourceProperties.getPasswordEncryptionSeed(),
                additionalSalt)).isEqualTo(password);
    }

    @Test
    public void encryptPasswordTest2() {
        ***REMOVED***;
        String additionalSalt = "containerHost";
        String encryptedPassword = EncryptionUtils.encryptWithAes(password,
                resourceProperties.getPasswordEncryptionSeed(), additionalSalt);
        log.info("encryptedPassword: " + encryptedPassword);

        assertThat(EncryptionUtils.decryptWithAes(encryptedPassword, resourceProperties.getPasswordEncryptionSeed(),
                additionalSalt)).isEqualTo(password);
    }

    @Test
    public void decryptPasswordTest() {
        String enPassword = "6lbMYUmgQbSEHschrWjskxsUF6hh02IWInyBHxtgn1A=";
        String additionalSalt = "service_mgmt";
        String password = EncryptionUtils.decryptWithAes(enPassword, resourceProperties.getPasswordEncryptionSeed(),
                additionalSalt);
        log.info("decryptedPassword: " + password);

        assertThat(EncryptionUtils.encryptWithAes(password, resourceProperties.getPasswordEncryptionSeed(),
                additionalSalt)).isEqualTo(enPassword);
    }

}
