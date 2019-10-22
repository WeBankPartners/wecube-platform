package com.webank.wecube.platform.core.utils.constant;

public enum DataModelState {
    Draft("draft"), Published("published");

    private String code;

    DataModelState(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DataModelState fromCode(String code) throws IllegalArgumentException {
        for (DataModelState dataModelState : values()) {
            if (dataModelState.getCode().equals(code)) {
                return dataModelState;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find the data model state from code %s", code));
    }
}
