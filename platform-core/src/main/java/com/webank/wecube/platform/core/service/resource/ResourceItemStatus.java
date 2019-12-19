package com.webank.wecube.platform.core.service.resource;

public enum ResourceItemStatus {
    NONE("none"),
    CREATED("created"),
    RUNNING("running"),
    STOPPED("stopped");

    private String code;

    private ResourceItemStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ResourceItemStatus fromCode(String code) {
        for (ResourceItemStatus value : values()) {
            if (NONE.equals(value))
                continue;

            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return NONE;
    }

}
