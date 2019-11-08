package com.webank.wecube.platform.auth.server.encryption;

import java.nio.charset.Charset;

import com.webank.wecube.platform.auth.server.common.util.StringUtilsEx;

/**
 * 
 * @author gavin
 *
 */
public final class EncryptionUtils {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /* --------------- Asymmetric start ----------------------------- */

    public static AsymmetricKeyPair initAsymmetricKeyPair() {
        return RSACoder.initKey();
    }

    public static byte[] encryptByPrivateKey(byte[] data, byte[] key) {
        return RSACoder.encryptByPrivateKey(data, key);
    }

    public static String encryptByPrivateKeyAsString(byte[] data, String sKey) {
        byte[] key = StringUtilsEx.decodeBase64(sKey);

        return StringUtilsEx.encodeBase64String(encryptByPrivateKey(data, key));
    }
    
    public static byte[] encryptByPublicKey(byte[] data, byte[] key) {
        return RSACoder.encryptByPublicKey(data, key);
    }

    public static String encryptByPublicKeyAsString(byte[] data, String sKey) {
        byte[] key = StringUtilsEx.decodeBase64(sKey);
        return StringUtilsEx.encodeBase64String(encryptByPublicKey(data, key));
    }

    
    // ----    decrypt -----
    public static byte[] decryptByPrivateKey(byte[] data, byte[] key) {
        return RSACoder.decryptByPrivateKey(data, key);
    }

    public static byte[] decryptByPrivateKeyAsString(byte[] data, String sKey) {
        byte[] key = StringUtilsEx.decodeBase64(sKey);
        return decryptByPrivateKey(data, key);
    }
    
    public static byte[] decryptByPublicKey(byte[] data, byte[] key) {
        return RSACoder.decryptByPublicKey(data, key);
    }

    public static byte[] decryptByPublicKeyAsString(byte[] data, String sKey) {
        byte[] key = StringUtilsEx.decodeBase64(sKey);
        return decryptByPublicKey(data, key);
    }
    
    /* --------------- Asymmetric end ----------------------------- */

    /* ---------------- Symmetric start -------------------------- */
    public static byte[] encrypt(byte[] data, byte[] key) {
        return DESCoder.encrypt(data, key);
    }

    public static String encryptWithBase64Key(byte[] data, String base64stringKey) {
        byte[] key = StringUtilsEx.decodeBase64(base64stringKey);
        return StringUtilsEx.encodeBase64String(encrypt(data, key));
    }

    public static String encryptStringWithBase64Key(String sdata, String base64stringKey) {
        byte[] key = StringUtilsEx.decodeBase64(base64stringKey);
        byte[] data = sdata.getBytes(DEFAULT_CHARSET);
        return StringUtilsEx.encodeBase64String(encrypt(data, key));
    }

    public static byte[] decrypt(byte[] data, byte[] key) {
        return DESCoder.decrypt(data, key);
    }

    public static String decryptWithBase64Key(byte[] data, String base64stringKey) {
        byte[] key = StringUtilsEx.decodeBase64(base64stringKey);
        return new String(decrypt(data, key), DEFAULT_CHARSET);
    }

    public static String decryptStringWithBase64Key(String base64stringData, String base64stringKey) {
        byte[] key = StringUtilsEx.decodeBase64(base64stringKey);
        byte[] data = StringUtilsEx.decodeBase64(base64stringData);
        return new String(decrypt(data, key), DEFAULT_CHARSET);
    }
    /* ---------------- Symmetric end -------------------------- */
}
