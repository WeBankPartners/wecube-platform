package com.webank.wecube.platform.core.dto.plugin;

public class InputParameterDefinitionDto {
    public static final String DATA_TYPE_STRING = "string";
    public static final String DATA_TYPE_NUMBER = "number";

    public static final String DEFAULT_VALUE_DATA_TYPE_STRING = "";
    public static final int DEFAULT_VALUE_DATA_TYPE_NUMBER = 0;

    private PluginConfigInterfaceParameterDto inputParameter;
    private Object inputParameterValue;

    public PluginConfigInterfaceParameterDto getInputParameter() {
        return inputParameter;
    }

    public void setInputParameter(PluginConfigInterfaceParameterDto inputParameter) {
        this.inputParameter = inputParameter;
    }

    public Object getInputParameterValue() {
        return inputParameterValue;
    }

    public void setInputParameterValue(Object inputParameterValue) {
        this.inputParameterValue = inputParameterValue;
    }

    public static String getDataTypeString() {
        return DATA_TYPE_STRING;
    }

    public static String getDataTypeNumber() {
        return DATA_TYPE_NUMBER;
    }

    public static String getDefaultValueDataTypeString() {
        return DEFAULT_VALUE_DATA_TYPE_STRING;
    }

    public static int getDefaultValueDataTypeNumber() {
        return DEFAULT_VALUE_DATA_TYPE_NUMBER;
    }

    public Object getExpectedValue() {
        if (inputParameterValue == null) {
            return determineEmptyValue();
        }
        return inputParameterValue;
    }

    private Object determineEmptyValue() {
        if (DATA_TYPE_STRING.equalsIgnoreCase(inputParameter.getType())) {
            return DEFAULT_VALUE_DATA_TYPE_STRING;
        }

        if (DATA_TYPE_NUMBER.equalsIgnoreCase(inputParameter.getType())) {
            return DEFAULT_VALUE_DATA_TYPE_NUMBER;
        }

        return DEFAULT_VALUE_DATA_TYPE_STRING;
    }

}
