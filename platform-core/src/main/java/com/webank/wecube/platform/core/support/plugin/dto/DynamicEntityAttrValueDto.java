package com.webank.wecube.platform.core.support.plugin.dto;

public class DynamicEntityAttrValueDto {
    private String attrDefId;
    private String attrName;
    private String dataType;
    private Object dataValue;

    public String getAttrDefId() {
        return attrDefId;
    }

    public void setAttrDefId(String attrDefId) {
        this.attrDefId = attrDefId;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Object getDataValue() {
        return dataValue;
    }

    public void setDataValue(Object dataValue) {
        this.dataValue = dataValue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DynamicEntityAttrValueDto [attrDefId=");
        builder.append(attrDefId);
        builder.append(", attrName=");
        builder.append(attrName);
        builder.append(", dataType=");
        builder.append(dataType);
        builder.append(", dataValue=");
        builder.append(dataValue);
        builder.append("]");
        return builder.toString();
    }

}
