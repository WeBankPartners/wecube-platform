package com.webank.wecube.platform.core.model.datamodel;

public class DataModelExpressionToRootData {
    private String dataModelExpression;
    private String rootData;

    public DataModelExpressionToRootData(String dataModelExpression, String rootData) {
        this.dataModelExpression = dataModelExpression;
        this.rootData = rootData;
    }

    public String getDataModelExpression() {
        return dataModelExpression;
    }

    public void setDataModelExpression(String dataModelExpression) {
        this.dataModelExpression = dataModelExpression;
    }

    public String getRootData() {
        return rootData;
    }

    public void setRootData(String rootData) {
        this.rootData = rootData;
    }
}
