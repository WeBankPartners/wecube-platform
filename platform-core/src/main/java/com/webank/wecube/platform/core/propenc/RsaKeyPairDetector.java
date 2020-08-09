package com.webank.wecube.platform.core.propenc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gavin
 *
 */
public class RsaKeyPairDetector {
    private static final Logger log = LoggerFactory.getLogger(RsaKeyPairDetector.class);

    private static final String DEF_RSA_KEY_FILENAME = "rsa_key";
    private static final String DEF_RSA_PUB_KEY_FILENAME = "rsa_key.pub";

    private File publicKeyFile;
    private File privateKeyFile;

    public RsaKeyPairDetector() {

    }

    public RsaKeyPairDetector(String rsaPubKey, String rsaKey) {
        File pubKeyFile = null;
        if (!StringUtils.isBlank(rsaPubKey)) {
            pubKeyFile = new File(rsaPubKey.trim());
        }

        File privKeyFile = null;
        if (!StringUtils.isBlank(rsaKey)) {
            privKeyFile = new File(rsaKey.trim());
        }

        this.publicKeyFile = pubKeyFile;
        this.privateKeyFile = privKeyFile;
    }

    public RsaKeyPairDetector(File rsaPublicKeyFile, File rsaPrivateKeyFile) {
        super();
        this.publicKeyFile = rsaPublicKeyFile;
        this.privateKeyFile = rsaPrivateKeyFile;
    }

    public RsaKeyPair detectRsaKeyPair() {
        log.info("try to detect rsa key");
        if (privateKeyFile != null && publicKeyFile != null) {
            String privKey = tryFindPrivateKeyFromExternal();
            String pubKey = tryFindPublicKeyFromExternal();
            return RsaKeyPairBuilder.withPublicKey(pubKey).withPrivateKey(privKey).build();
        } else if (privateKeyFile != null && publicKeyFile == null) {
            String privKey = tryFindPrivateKeyFromExternal();
            return RsaKeyPairBuilder.withPublicKey(null).withPrivateKey(privKey).build();
        } else if (privateKeyFile == null && publicKeyFile != null) {
            String pubKey = tryFindPublicKeyFromExternal();
            return RsaKeyPairBuilder.withPublicKey(pubKey).withPrivateKey(null).build();
        } else {
            String privKey = tryFindPrivateKeyFromDefault();
            String pubKey = tryFindPublicKeyFromDefault();
            return RsaKeyPairBuilder.withPublicKey(pubKey).withPrivateKey(privKey).build();
        }
    }

    public String tryFindPrivateKey() {
        if (this.privateKeyFile != null) {
            log.info("try to read private key from:{}", privateKeyFile.getAbsolutePath());
            return tryFindPrivateKeyFromExternal();
        } else {
            log.info("private key not provided and try to find default one.");
            return tryFindPrivateKeyFromDefault();
        }
    }

    public String tryFindPublicKey() {
        if (this.publicKeyFile != null) {
            log.info("try to read public key from:{}", publicKeyFile.getAbsolutePath());
            return tryFindPublicKeyFromExternal();
        } else {
            log.info("public key not provided and try to find default one.");
            return tryFindPublicKeyFromDefault();
        }
    }

    private String tryFindPrivateKeyFromDefault() {
        try (InputStream input = this.getClass().getResourceAsStream(DEF_RSA_KEY_FILENAME)) {
            return readInputStream(input);
        } catch (IOException e) {
            String msg = "Failed to read default private key";
            log.error(msg, e);
            throw new EncryptionException(msg);
        }
    }

    private String tryFindPrivateKeyFromExternal() {
        if (!this.privateKeyFile.exists()) {
            log.error("Private key does not exist,filepath={}", this.privateKeyFile.getAbsolutePath());
            String msg = String.format("Private key {%s} does not exist.", this.privateKeyFile.getAbsolutePath());
            throw new EncryptionException(msg);
        }

        try (FileInputStream input = new FileInputStream(privateKeyFile)) {
            return readInputStream(input);
        } catch (IOException e) {
            log.error("errors while reading private key", e);
            String msg = String.format("Failed to read private key {%s}.", this.privateKeyFile.getAbsolutePath());
            throw new EncryptionException(msg);
        }
    }

    private String tryFindPublicKeyFromDefault() {
        try (InputStream input = this.getClass().getResourceAsStream(DEF_RSA_PUB_KEY_FILENAME)) {
            return readInputStream(input);
        } catch (IOException e) {
            String msg = "Failed to read default public key";
            log.error(msg, e);
            throw new EncryptionException(msg);
        }
    }

    private String tryFindPublicKeyFromExternal() {
        if (!this.publicKeyFile.exists()) {
            log.error("Public key does not exist,filepath={}", this.publicKeyFile.getAbsolutePath());
            String msg = String.format("Public key {%s} does not exist.", this.publicKeyFile.getAbsolutePath());
            throw new EncryptionException(msg);
        }

        try (FileInputStream input = new FileInputStream(publicKeyFile)) {
            return readInputStream(input);
        } catch (IOException e) {
            log.error("errors while reading public key", e);
            String msg = String.format("Failed to read public key {%s}.", this.publicKeyFile.getAbsolutePath());
            throw new EncryptionException(msg);
        }
    }

    private String readInputStream(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            throw new IllegalArgumentException();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, RsaEncryptor.DEF_CHARSET));
        String sLine = null;
        StringBuilder content = new StringBuilder();
        while ((sLine = br.readLine()) != null) {
            if (sLine.startsWith("-")) {
                continue;
            }

            content.append(sLine.trim());
        }

        return content.toString();
    }
}
