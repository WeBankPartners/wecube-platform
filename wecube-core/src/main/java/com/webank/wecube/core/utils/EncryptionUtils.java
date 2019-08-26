package com.webank.wecube.core.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.DigestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptionUtils {
    public static String generateKeyFromSeedAndSalt(String seed, String additionalSalt) {
        return String.format("%16s", DigestUtils.md5DigestAsHex((seed + additionalSalt).getBytes()).substring(0, 15));
    }

    public static String encryptWithAes(String password, String seed, String additionalSalt) throws Exception {
        String key = generateKeyFromSeedAndSalt(seed, additionalSalt);
        String encrytedPassword = null;
        if (key == null) {
            String message = "Failed to encrypt password as key is missing.";
            log.error(message);
            throw new Exception(message);
        }

        if (key.length() != 16) {
            String message = String.format("Failed to encrypt password, length of key [%s] must be 16.", key);
            log.error(message);
            throw new Exception(message);
        }

        try {
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(password.getBytes("utf-8"));
            encrytedPassword = new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Failed to encrypt password, meet error {%s}", e.getMessage());
            throw e;
        }

        return encrytedPassword;
    }

    public static String decryptWithAes(String encryptedPassword, String seed, String additionalSalt) throws Exception {
        String key = generateKeyFromSeedAndSalt(seed, additionalSalt);
        String password = null;
        if (key == null) {
            String message = "Failed to decrypt password as key is missing.";
            log.error(message);
            throw new Exception(message);
        }

        if (key.length() != 16) {
            String message = String.format("Failed to encrypt password, length of key [%s] must be 16.", key);
            log.error(message);
            throw new Exception(message);
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
            log.error("Failed to decrypt password, meet error [%s]", e.getMessage());
            throw e;
        }
        return password;
    }

}
