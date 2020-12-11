package com.webank.wecube.platform.core.service.dme;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityQueryCriteria implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 3062150549558722047L;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attrName == null) ? 0 : attrName.hashCode());
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
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
        EntityQueryCriteria other = (EntityQueryCriteria) obj;
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
        return true;
    }

}
