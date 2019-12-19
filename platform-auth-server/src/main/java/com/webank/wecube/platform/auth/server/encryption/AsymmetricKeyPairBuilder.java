package com.webank.wecube.platform.auth.server.encryption;

public class AsymmetricKeyPairBuilder {
    
    private String privateKey;
    private String publicKey;
    
    public static AsymmetricKeyPairBuilder withPublicKey(String publicKey){
        AsymmetricKeyPairBuilder b = new AsymmetricKeyPairBuilder();
        b.setPublicKey(publicKey);
        
        return b;
    }
    
    public AsymmetricKeyPairBuilder withPrivateKey(String privateKey){
        this.setPrivateKey(privateKey);
        return this;
    }
    
    public AsymmetricKeyPair build(){
        return new AsymmetricKeyPair(this.publicKey, this.privateKey);
    }

    private void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    private void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    

}
