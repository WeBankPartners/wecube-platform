package com.webank.wecube.platform.core.utils.constant;

public enum DataModelDataType {
    Ref("ref"), String("str"), Integer("int"), Timestamp("timestamp");

    private String code;

    DataModelDataType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DataModelDataType fromCode(String code) throws IllegalArgumentException {
        for (DataModelDataType dataModelDataType : values()) {
            if (dataModelDataType.getCode().equals(code)) {
                return dataModelDataType;
            }
        }
        throw new IllegalArgumentException(java.lang.String.format("Cannot find the data model data type from code %s", code));
    }
}
