package com.webank.wecube.platform.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptionUtilsTest extends DatabaseBasedTest {

    @Autowired
    private ResourceProperties resourceProperties;

    @Test
    public void encryptPasswordTest() {
        ***REMOVED***;
        String additionalSalt = "mysqlHost";
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
        String password = EncryptionUtils.decryptWithAes(enPassword,
                resourceProperties.getPasswordEncryptionSeed(), additionalSalt);
        log.info("decryptedPassword: " + password);
        
        assertThat(EncryptionUtils.encryptWithAes(password, resourceProperties.getPasswordEncryptionSeed(),
                additionalSalt)).isEqualTo(enPassword);
    }

}
