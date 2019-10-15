package com.webank.wecube.platform.auth.server.encryption;

public class AsymmetricKeyPair {
    
    private final String privateKey;
    
    private final String publicKey;

    AsymmetricKeyPair( String publicKey, String privateKey) {
        super();
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
