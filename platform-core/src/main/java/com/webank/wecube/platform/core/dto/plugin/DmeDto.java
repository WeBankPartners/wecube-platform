package com.webank.wecube.platform.core.dto.plugin;

public class DmeDto {
    private String dataModelExpression;

    public String getDataModelExpression() {
        return dataModelExpression;
    }

    public void setDataModelExpression(String dataModelExpression) {
        this.dataModelExpression = dataModelExpression;
    }

    public DmeDto(String dataModelExpression) {
        super();
        this.dataModelExpression = dataModelExpression;
    }

    public DmeDto() {
    }

}
