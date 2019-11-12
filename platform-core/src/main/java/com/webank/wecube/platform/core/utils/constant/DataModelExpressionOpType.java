package com.webank.wecube.platform.core.utils.constant;

public enum DataModelExpressionOpType {
    ReferenceBy("~"), ReferenceTo("-");

    private String code;

    DataModelExpressionOpType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DataModelExpressionOpType fromCode(String code) throws IllegalArgumentException {
        for (DataModelExpressionOpType dataModelDataType : values()) {
            if (dataModelDataType.getCode().equals(code)) {
                return dataModelDataType;
            }
        }
        throw new IllegalArgumentException(java.lang.String.format("Cannot find the data model expression operation type from code %s", code));
    }
}
