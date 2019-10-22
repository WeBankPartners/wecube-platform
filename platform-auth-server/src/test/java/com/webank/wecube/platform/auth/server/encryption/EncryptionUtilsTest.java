package com.webank.wecube.platform.auth.server.encryption;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;

public class EncryptionUtilsTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String privateKey = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAlblsUUH6TLYuHp0UjxY0ahljznG2Ik/bsuQHL3oBTRmbNKYlIHBC4gFKYB0K/ULFvzJbfxCJMKDKD3DpoIec7wIDAQABAkBBZDWBm85E2MB10GcdQzZrLGGh1ZoVjElI7TySKLgOwraHM/SA4kbYJcKEn2AoqSccaUeogWPYhirsObWNWUZhAiEA4WLqPOWZNbTQRYms0UbMZcRyN7dBnFnCyq/wdoI3bTECIQCqD5pxHfQPD1TcI8JL1SnzozRfnefpQOxdx+gcaD6kHwIhALf1l0A7GjD2suN++poZki0iCSOmpJuru8zZi4f+wqSRAiB9YoO8YxlPAT7QEI1w2/nSaMJ0vTgAAp5DhuDcEQAegQIgYKlMPS9E3m2FDsVIF/bvBcOq23Z8pqIGLBO28B0RSVI=";
    
    private String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJW5bFFB+ky2Lh6dFI8WNGoZY85xtiJP27LkBy96AU0ZmzSmJSBwQuIBSmAdCv1Cxb8yW38QiTCgyg9w6aCHnO8CAwEAAQ==";

    @Test
    public void testInitAsymmetricKeyPair() {
        AsymmetricKeyPair keyPair = EncryptionUtils.initAsymmetricKeyPair();
        Assert.assertNotNull(keyPair);

        log.info("privateKey:{}", keyPair.getPrivateKey());
        log.info("publicKey:{}", keyPair.getPublicKey());
    }

    @Test
    public void testEncryptByPrivateKeyAndDecrptByPublicKey() {
        String systemCode = "HTTP-MOCK";
        String nonce = "123";
        String password = String.format("%s:%s", systemCode, nonce);
        
        String cipherPassword = EncryptionUtils.encryptByPrivateKeyAsString(password.getBytes(EncryptionUtils.UTF8),
                privateKey);
        
        Assert.assertNotNull(cipherPassword);
        
        log.info("password:{}", cipherPassword);
        
        String plainPassword = new String(
                EncryptionUtils.decryptByPublicKeyAsString(StringUtilsEx.decodeBase64(cipherPassword), publicKey),
                EncryptionUtils.UTF8);
        
        Assert.assertNotNull(plainPassword);
        
        Assert.assertEquals(password, plainPassword);
        
        
    }

    @Test
    public void testEncryptByPrivateKeyAsString() {
    }

    @Test
    public void testEncryptByPublicKey() {
    }

    @Test
    public void testEncryptByPublicKeyAsString() {
    }

    @Test
    public void testDecryptByPrivateKey() {
    }

    @Test
    public void testDecryptByPrivateKeyAsString() {
    }

    @Test
    public void testDecryptByPublicKey() {
    }

    @Test
    public void testDecryptByPublicKeyAsString() {
    }

    @Test
    public void testEncrypt() {
    }

    @Test
    public void testEncryptWithBase64Key() {
    }

    @Test
    public void testEncryptStringWithBase64Key() {
    }

    @Test
    public void testDecrypt() {
    }

    @Test
    public void testDecryptWithBase64Key() {
    }

    @Test
    public void testDecryptStringWithBase64Key() {
    }

}
