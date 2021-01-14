package com.webank.wecube.platform.core.support.itsdanger;

public class ItsDangerInstanceInfoDto {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ItsDangerInstanceInfoDto [id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }

}
