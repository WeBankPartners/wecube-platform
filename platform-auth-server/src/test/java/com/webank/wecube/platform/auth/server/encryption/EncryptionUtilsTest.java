package com.webank.wecube.platform.auth.server.encryption;

import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;


public class EncryptionUtilsTest {

    @Test
    public void testRSAEncryption() {
        String originalText = "123abcddsjgksakgjklasjgkljsaklgjkladsjfkgljsakdfgkdsajkgjdaskgjkadsljgkldasjgl";
        
        long st = System.currentTimeMillis();
        
        for(int i = 0; i < 10; i++){
            AsymmetricKeyPair keyPair = EncryptionUtils.initAsymmetricKeyPair();
            
            System.out.println("keyPair  pub " + keyPair.getPublicKey());
            System.out.println("keyPair privat " + keyPair.getPrivateKey());
            String cipherText = EncryptionUtils.encryptByPrivateKeyAsString(originalText.getBytes(Charset.forName("iso-8859-1")), keyPair.getPrivateKey());
            
            System.out.println("cipherText " + cipherText);
            String plainText = EncryptionUtils.decryptByPublicKeyAsString(StringUtilsEx.decodeBase64(cipherText), keyPair.getPublicKey());
            System.out.println("plainText "+ plainText);
            Assert.assertEquals(originalText, plainText);
        }
        
        long ed = System.currentTimeMillis();
        
        System.out.println("eclapse:"+ (ed-st));
    }

}
