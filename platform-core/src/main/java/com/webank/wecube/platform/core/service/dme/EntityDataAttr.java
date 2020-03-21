package com.webank.wecube.platform.core.service.dme;

public class EntityDataAttr {
    private String attrName;
    private Object attrValue;

    public EntityDataAttr() {
        super();
    }

    public EntityDataAttr(String attrName, Object value) {
        super();
        this.attrName = attrName;
        this.attrValue = value;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public Object getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(Object value) {
        this.attrValue = value;
    }

}
