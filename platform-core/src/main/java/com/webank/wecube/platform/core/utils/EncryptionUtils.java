package com.webank.wecube.platform.core.utils;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import com.webank.wecube.platform.core.commons.WecubeCoreException;

public class EncryptionUtils {
    private static final Logger log = LoggerFactory.getLogger(EncryptionUtils.class);
    private static final String INIT_VECTOR = "encriptionVector";

    public static String generateKeyFromSeedAndSalt(String seed, String additionalSalt) {
        return String.format("%16s", DigestUtils.md5DigestAsHex((seed + additionalSalt).getBytes()).substring(0, 15));
    }

    public static String encryptWithAes(String password, String seed, String additionalSalt) {
        String key = generateKeyFromSeedAndSalt(seed, additionalSalt);
        String encrytedPassword = null;
        if (key == null) {
            String message = "Failed to encrypt password as key is missing.";
            log.error(message);
            throw new WecubeCoreException(message);
        }

        if (key.length() != 16) {
            String message = String.format("Failed to encrypt password, length of key [%s] must be 16.", key);
            log.error(message);
            throw new WecubeCoreException(message);
        }

        try {
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(password.getBytes("utf-8"));
            encrytedPassword = new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            String message = String.format("Failed to encrypt password, meet error [%s]", e.getMessage());
            log.error(message);
            throw new WecubeCoreException(message);
        }

        return encrytedPassword;
    }

    public static String decryptWithAes(String encryptedPassword, String seed, String additionalSalt) {
        String key = generateKeyFromSeedAndSalt(seed, additionalSalt);
        String password = null;
        if (key == null) {
            String message = "Failed to decrypt password as key is missing.";
            log.error(message);
            throw new WecubeCoreException(message);
        }

        if (key.length() != 16) {
            String message = String.format("Failed to encrypt password, length of key [%s] must be 16.", key);
            log.error(message);
            throw new WecubeCoreException(message);
        }

        try {
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted = new Base64().decode(encryptedPassword);
            byte[] original = cipher.doFinal(encrypted);
            String originalString = new String(original, "utf-8");
            password = originalString;
        } catch (Exception e) {
            String message = String.format("Failed to decrypt password, meet error [%s]", e.getMessage());
            log.error(message);
            throw new WecubeCoreException(message);
        }
        return password;
    }

    public static String encryptWithAesCbc(String plainData, String seed) {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        String encrytedData = null;
        if (seed == null) {
            String message = "Failed to encrypt password as key is missing.";
            log.error(message);
            throw new WecubeCoreException(message);
        }

        try {
            byte[] raw = seed.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("utf-8"));
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(plainData.getBytes("utf-8"));
            encrytedData = new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            String message = String.format("Failed to encrypt password, meet error [%s]", e.getMessage());
            log.error(message);
            throw new WecubeCoreException(message);
        }

        return encrytedData;
    }

    public static String decryptWithAesCbc(String encryptedData, String seed) {
        String password = null;
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        if (seed == null) {
            String message = "Failed to decrypt password as key is missing.";
            log.error(message);
            throw new WecubeCoreException(message);
        }

        try {
            byte[] raw = seed.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("utf-8"));
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted = new Base64().decode(encryptedData);
            byte[] original = cipher.doFinal(encrypted);
            String originalString = new String(original, "utf-8");
            password = originalString;
        } catch (Exception e) {
            String message = String.format("Failed to decrypt password, meet error [%s]", e.getMessage());
            log.error(message);
            throw new WecubeCoreException(message);
        }
        return password;
    }
}
