package com.webank.wecube.platform.auth.server.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleSubSystemDto implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5229760225564596993L;
    private String id;
    private String name;
    private String systemCode;
    private String description;
    private String apikey;
    private boolean active;
    private boolean blocked;
    
    private String pubKey;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SimpleSubSystemDto [id=");
        builder.append(id);
        builder.append(", name=");
        builder.append(name);
        builder.append(", systemCode=");
        builder.append(systemCode);
        builder.append(", description=");
        builder.append(description);
        builder.append(", apikey=");
        builder.append(apikey);
        builder.append(", active=");
        builder.append(active);
        builder.append(", blocked=");
        builder.append(blocked);
        builder.append("]");
        return builder.toString();
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }
    
    

}
