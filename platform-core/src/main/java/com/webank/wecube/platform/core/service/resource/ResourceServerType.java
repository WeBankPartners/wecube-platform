package com.webank.wecube.platform.core.service.resource;

public enum ResourceServerType {
    NONE("none"),
    S3("s3"),
    MYSQL("mysql"),
    DOCKER("docker");

    private String code;

    private ResourceServerType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ResourceServerType fromCode(String code) {
        for (ResourceServerType value : values()) {
            if (NONE.equals(value))
                continue;

            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return NONE;
    }

}
