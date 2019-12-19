package com.webank.wecube.platform.core.service.resource;

public enum ResourceServerStatus {
    NONE("none"),
    ACTIVE("active"),
    INACTIVE("inactive");

    private String code;

    private ResourceServerStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ResourceServerStatus fromCode(String code) {
        for (ResourceServerStatus value : values()) {
            if (NONE.equals(value))
                continue;

            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return NONE;
    }

}
