package com.webank.wecube.platform.auth.server.dto;

import java.io.Serializable;

public class SubSystemTokenDto implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7294867094805477547L;
    private String systemCode;
    private String accessToken;
    private String expireDate;// 20200815
    private String createDate;// 20200515
    private String nonce;

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

}
