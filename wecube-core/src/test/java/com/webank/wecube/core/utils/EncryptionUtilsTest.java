package com.webank.wecube.core.utils;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
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

    @Test
    public void genRandomPasswordSuccess() {
        String nowTimeString = new Date().toString();
        System.out.println(System.currentTimeMillis());
        String md5String = genRandomPassword(nowTimeString);
        System.out.println(md5String);
    }

    private String genRandomPassword(String nowTimeString) {
        return DigestUtils.md5Hex(nowTimeString).substring(0, 16);
    }
}
