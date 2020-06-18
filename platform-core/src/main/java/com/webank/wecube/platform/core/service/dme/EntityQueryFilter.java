package com.webank.wecube.platform.core.service.dme;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author gavin
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityQueryFilter {
    public static final String OP_EQUALS = "eq";
    public static final String OP_NOT_EQUALS = "neq";
    public static final String OP_IN = "in";
    public static final String OP_LIKE = "like";
    public static final String OP_GREAT_THAN = "gt";
    public static final String OP_LESS_THAN = "lt";
    public static final String OP_IS = "is";
    public static final String OP_IS_NOT = "isnot";
    private String attrName;
    private String op;
    private Object condition;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public Object getCondition() {
        return condition;
    }

    public void setCondition(Object condition) {
        this.condition = condition;
    }

    

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[attrName=");
        builder.append(attrName);
        builder.append(", op=");
        builder.append(op);
        builder.append(", condition=");
        builder.append(condition);
        builder.append("]");
        return builder.toString();
    }
    
    
}
