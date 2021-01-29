package com.webank.wecube.platform.core.service.dme;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author gavin
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityQueryFilter implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3043215832890660381L;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attrName == null) ? 0 : attrName.hashCode());
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((op == null) ? 0 : op.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntityQueryFilter other = (EntityQueryFilter) obj;
        if (attrName == null) {
            if (other.attrName != null) {
                return false;
            }
        } else if (!attrName.equals(other.attrName)) {
            return false;
        }
        if (condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!condition.equals(other.condition)) {
            return false;
        }
        if (op == null) {
            if (other.op != null) {
                return false;
            }
        } else if (!op.equals(other.op)) {
            return false;
        }
        return true;
    }

}
