package com.webank.wecube.platform.auth.server.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * 
 * @author gavin
 *
 */
final class DESCoder {

    private static final String CIPHER_ALGORITHM = "DES";

    private static final DESCoder _INSTANCE = new DESCoder();

    public static byte[] encrypt(byte[] data, byte[] key) {
        try {
            return _INSTANCE.doEncrypt(data, key);
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException e) {
            throw new EncryptionRuntimeException(e.getMessage());
        }
    }

    public static byte[] decrypt(byte[] data, byte[] key) {
        try {
            return _INSTANCE.doDecrypt(data, key);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException
                | IllegalBlockSizeException | BadPaddingException e) {
            throw new EncryptionRuntimeException(e.getMessage());
        }
    }

    private byte[] doEncrypt(byte[] data, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CIPHER_ALGORITHM);

        SecretKey securekey = keyFactory.generateSecret(desKey);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        cipher.init(Cipher.ENCRYPT_MODE, securekey, random);

        return cipher.doFinal(data);
    }

    public byte[] doDecrypt(byte[] data, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CIPHER_ALGORITHM);
        SecretKey securekey = keyFactory.generateSecret(desKey);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        return cipher.doFinal(data);

    }

}
