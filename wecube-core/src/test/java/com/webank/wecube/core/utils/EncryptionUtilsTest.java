package com.webank.wecube.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EncryptionUtilsTest {

    @Test
    public void whenEncryptPasswordWithSeedShouldSuccess() throws Exception {
        ***REMOVED***;
        String seed = "testSeed";
        String encryptedPassword = EncryptionUtils.encryptWithAes(password, seed, "testSalt");
        assertEquals("oqeTuFptLW5iqyvcTINErg==", encryptedPassword);
    }

    @Test
    public void whenDecryptPasswordWithSeedShouldSuccess() throws Exception {
        String encryptedPassword = "oqeTuFptLW5iqyvcTINErg==";
        String seed = "testSeed";
        String password = EncryptionUtils.decryptWithAes(encryptedPassword, seed, "testSalt");
        assertEquals("testPassword", password);
    }

}
