package com.webank.wecube.platform.core.support.itsdanger;

public class ItsDangerInstanceInfoDto {
    private String id;
    private String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ItsDangerInstanceInfoDto [id=");
        builder.append(id);
        builder.append(", displayName=");
        builder.append(displayName);
        builder.append("]");
        return builder.toString();
    }
    
    

}
