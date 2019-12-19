package com.webank.wecube.platform.core.service.resource;

public enum ResourceItemType {
    NONE("none"),
    S3_BUCKET("s3_bucket"),
    MYSQL_DATABASE("mysql_database"),
    DOCKER_CONTAINER("docker_container"),
    DOCKER_IMAGE("docker_image");

    private String code;

    private ResourceItemType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ResourceItemType fromCode(String code) {
        for (ResourceItemType value : values()) {
            if (NONE.equals(value))
                continue;

            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return NONE;
    }

}
