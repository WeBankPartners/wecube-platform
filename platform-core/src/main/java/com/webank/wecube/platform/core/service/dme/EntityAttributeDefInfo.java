package com.webank.wecube.platform.core.service.dme;

import com.webank.wecube.platform.core.utils.Constants;

public class EntityAttributeDefInfo {
    private String packageName;
    private String entityName;
    private String attrName;
    private String dataType;
    private String refPackageName;
    private String refEntityName;
    private String refAttributeName;
    private String mandatory;
    private String multiple;
    private String orderNo;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
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

    public String getRefPackageName() {
        return refPackageName;
    }

    public void setRefPackageName(String refPackageName) {
        this.refPackageName = refPackageName;
    }

    public String getRefEntityName() {
        return refEntityName;
    }

    public void setRefEntityName(String refEntityName) {
        this.refEntityName = refEntityName;
    }

    public String getRefAttributeName() {
        return refAttributeName;
    }

    public void setRefAttributeName(String refAttributeName) {
        this.refAttributeName = refAttributeName;
    }

    public String getMandatory() {
        return mandatory;
    }

    public void setMandatory(String mandatory) {
        this.mandatory = mandatory;
    }

    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public boolean isMandatory() {
        return Constants.DATA_MANDATORY_YES.equalsIgnoreCase(mandatory);
    }
    
    public boolean isMultiple() {
        return Constants.DATA_MULTIPLE.equalsIgnoreCase(multiple);
    }

}
