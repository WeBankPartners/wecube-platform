package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import java.util.LinkedList;
import java.util.List;

public class IntQueryOperateAggRequestDto {
    private Integer queryId;
    private String operation;
    private String queryName;
    private String queryDesc;
    private List<Criteria> criterias = new LinkedList<>();

    public static class Criteria {
        private String branchId;
        private int ciTypeId;
        private String ciTypeName;
        private List<CriteriaNode> routine = new LinkedList<>();
        private CriteriaCiTypeAttr attribute;

//        public Criteria() {
//        }

        public int getCiTypeId() {
            return ciTypeId;
        }

        public void setCiTypeId(int ciTypeId) {
            this.ciTypeId = ciTypeId;
        }

        public String getCiTypeName() {
            return ciTypeName;
        }

        public void setCiTypeName(String ciTypeName) {
            this.ciTypeName = ciTypeName;
        }

        public CriteriaCiTypeAttr getAttribute() {
            return attribute;
        }

        public void setAttribute(CriteriaCiTypeAttr attribute) {
            this.attribute = attribute;
        }

        public String getBranchId() {
            return branchId;
        }

        public void setBranchId(String branchId) {
            this.branchId = branchId;
        }

        public List<CriteriaNode> getRoutine() {
            return routine;
        }

        public void setRoutine(List<CriteriaNode> routine) {
            this.routine = routine;
        }
    }

    public static class CriteriaNode {
        private int ciTypeId;
        private Relationship parentRs;

        public CriteriaNode() {
        }

        public CriteriaNode(int ciTypeId) {
            this(ciTypeId, null);
        }

        public CriteriaNode(int ciTypeId, Relationship parentRs) {
            this.ciTypeId = ciTypeId;
            this.parentRs = parentRs;
        }

        public Integer getCiTypeId() {
            return ciTypeId;
        }

        public void setCiTypeId(int ciTypeId) {
            this.ciTypeId = ciTypeId;
        }

        public Relationship getParentRs() {
            return parentRs;
        }

        public void setParentRs(Relationship parentRs) {
            this.parentRs = parentRs;
        }
    }

    public static class CriteriaCiTypeAttr {
        private Integer attrId;
        private boolean isCondition;
        private boolean isDisplayed;

        public CriteriaCiTypeAttr() {
        }

        public CriteriaCiTypeAttr(Integer attrId, boolean isCondition, boolean isDisplayed) {
            this.attrId = attrId;
            this.isCondition = isCondition;
            this.isDisplayed = isDisplayed;
        }

        public Integer getAttrId() {
            return attrId;
        }

        public void setAttrId(Integer attrId) {
            this.attrId = attrId;
        }

        public boolean isCondition() {
            return isCondition;
        }

        public void setCondition(boolean isCondition) {
            this.isCondition = isCondition;
        }

        public boolean isDisplayed() {
            return isDisplayed;
        }

        public void setDisplayed(boolean isDisplayed) {
            this.isDisplayed = isDisplayed;
        }
    }

    public Integer getQueryId() {
        return queryId;
    }

    public void setQueryId(Integer queryId) {
        this.queryId = queryId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryDesc() {
        return queryDesc;
    }

    public void setQueryDesc(String queryDesc) {
        this.queryDesc = queryDesc;
    }

    public List<Criteria> getCriterias() {
        return criterias;
    }

    public void setCriterias(List<Criteria> criterias) {
        this.criterias = criterias;
    }

}
