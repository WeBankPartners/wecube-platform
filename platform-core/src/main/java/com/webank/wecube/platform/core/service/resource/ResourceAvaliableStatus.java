package com.webank.wecube.platform.core.service.resource;

public enum ResourceAvaliableStatus {
    NONE("none"),
    CREATED("created"),
    RUNNING("running"),
    STOPPED("stopped");

    private String code;

    private ResourceAvaliableStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ResourceAvaliableStatus fromCode(String code) {
        for (ResourceAvaliableStatus value : values()) {
            if (NONE.equals(value))
                continue;

            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return NONE;
    }

}
