package com.webank.wecube.platform.core.dto.plugin;

public enum FilterOperator {
    NONE("none"),
    IN("in"),
    CONTAINS("contains"),
    EQUAL("eq"),
    GREATER("gt"),
    LESS("lt"),
    NOT_EQUAL("ne"),
    NOT_NULL("notNull"),
    NULL("null");

    private String code;

    private FilterOperator(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Get FilterOperator from code
     *
     * @param code The input FilterOperator code
     * @return The matching enum value. NONE if there is not matching enum value
     */
    static public FilterOperator fromCode(String code) {
        for (FilterOperator operator : values()) {
            if (NONE.equals(operator))
                continue;

            if (operator.getCode().equals(code)) {
                return operator;
            }
        }
        return NONE;
    }
}
