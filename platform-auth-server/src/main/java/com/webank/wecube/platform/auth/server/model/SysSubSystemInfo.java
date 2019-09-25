package com.webank.wecube.platform.auth.server.model;

public class SysSubSystemInfo {
    private Long id;

    private String name;

    private String systemCode;

    private String apiKey;

    private String pubApiKey;

    private Boolean active;

    private Boolean blocked;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPubApiKey() {
        return pubApiKey;
    }

    public void setPubApiKey(String pubApiKey) {
        this.pubApiKey = pubApiKey;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

}
