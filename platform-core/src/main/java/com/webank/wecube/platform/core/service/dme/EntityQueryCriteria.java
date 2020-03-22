package com.webank.wecube.platform.core.service.dme;

public class EntityQueryCriteria {

    private String attrName;
    private String condition;

    public EntityQueryCriteria() {
        super();
    }

    public EntityQueryCriteria(String attrName, String condition) {
        super();
        this.attrName = attrName;
        this.condition = condition;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[attrName=");
        builder.append(attrName);
        builder.append(", condition=");
        builder.append(condition);
        builder.append("]");
        return builder.toString();
    }
    
    

}
