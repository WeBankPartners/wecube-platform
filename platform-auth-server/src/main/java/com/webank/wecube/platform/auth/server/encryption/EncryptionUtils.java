package com.webank.wecube.platform.auth.server.encryption;

import java.nio.charset.Charset;

import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;

public final class EncryptionUtils {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static AsymmetricKeyPair initAsymmetricKeyPair() {
        return RSACoder.initKey();
    }

    public static byte[] decryptByPrivateKey(byte[] data, byte[] key) {
        return RSACoder.decryptByPrivateKey(data, key);
    }

    public static byte[] encryptByPrivateKey(byte[] data, byte[] key) {
        return RSACoder.encryptByPrivateKey(data, key);
    }

    public static byte[] decryptByPublicKey(byte[] data, byte[] key) {
        return RSACoder.decryptByPublicKey(data, key);
    }

    public static byte[] encryptByPublicKey(byte[] data, byte[] key) {
        return RSACoder.encryptByPublicKey(data, key);
    }

    public static String decryptByPrivateKeyAsString(byte[] data, String sKey) {
        byte[] key = StringUtilsEx.decodeBase64(sKey);
        byte[] plainData = decryptByPrivateKey(data, key);
        return new String(plainData, DEFAULT_CHARSET);
    }

    public static String encryptByPrivateKeyAsString(byte[] data, String sKey) {
        byte[] key = StringUtilsEx.decodeBase64(sKey);

        return StringUtilsEx.encodeBase64String(encryptByPrivateKey(data, key));
    }

    public static String decryptByPublicKeyAsString(byte[] data, String sKey) {
        byte[] key = StringUtilsEx.decodeBase64(sKey);
        byte[] plainData = decryptByPublicKey(data, key);
        return new String(plainData, DEFAULT_CHARSET);
    }

    public static String encryptByPublicKeyAsString(byte[] data, String sKey) {
        byte[] key = StringUtilsEx.decodeBase64(sKey);
        return StringUtilsEx.encodeBase64String(encryptByPublicKey(data, key));
    }
}
