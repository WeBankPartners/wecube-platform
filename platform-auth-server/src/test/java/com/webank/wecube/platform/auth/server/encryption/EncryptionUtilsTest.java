package com.webank.wecube.platform.auth.server.encryption;

import java.security.SecureRandom;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;

public class EncryptionUtilsTest {
    private Logger log = LoggerFactory.getLogger(EncryptionUtilsTest.class);

    @Test
    public void testRSAEncryption() {
        byte[] originalData = SecureRandom.getSeed(6);
        
        String originalText = StringUtilsEx.encodeBase64String(originalData);
        
        log.info("originalText {}", originalText);

        long st = System.currentTimeMillis();

        for (int i = 0; i < 1; i++) {
            AsymmetricKeyPair keyPair = EncryptionUtils.initAsymmetricKeyPair();

            log.info("keyPair  pub {}", keyPair.getPublicKey());
            log.info("keyPair privat " + keyPair.getPrivateKey());
            String cipherText = EncryptionUtils.encryptByPrivateKeyAsString(
                    originalData, keyPair.getPrivateKey());

            log.info("cipherText " + cipherText);
            byte[] plainData = EncryptionUtils.decryptByPublicKeyAsString(StringUtilsEx.decodeBase64(cipherText),
                    keyPair.getPublicKey());
            String plainText = StringUtilsEx.encodeBase64String(plainData);
            log.info("plainText " + plainText);
            Assert.assertEquals(originalText, plainText);
        }

        long ed = System.currentTimeMillis();

        log.info("eclapse:" + (ed - st));
    }

    @Test
    public void testDESEncryption() {
        String sKey = "umadmin:" + SecureRandom.getSeed(10);

        log.info("sKey:{}", sKey);
        String base64stringKey = StringUtilsEx.encodeBase64String(sKey.getBytes(EncryptionUtils.DEFAULT_CHARSET));
        
        String originalText = "I like DES vary much!中国人";
        
        log.info("original text:{}", originalText);
        
        String cipherTextBase64 = EncryptionUtils.encryptStringWithBase64Key(originalText, base64stringKey);
        
        log.info("cipher text:{}", cipherTextBase64);
        
        String plainText = EncryptionUtils.decryptStringWithBase64Key(cipherTextBase64, base64stringKey);
        
        log.info("plain text:{}", plainText);
        
        Assert.assertEquals(originalText, plainText);
    }

}
