package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class IntegrationQueryDto {
    private String name;
    private Integer ciTypeId;
    private List<Integer> attrs = new LinkedList<>();
    private List<String> attrAliases = new LinkedList<>();

    //relation with parent node, it is not needed in root node.
    private Relationship parentRs;
    private List<IntegrationQueryDto> children = new LinkedList<>();

    private List<String> aggKeyNames = new ArrayList<>();
    private List<String> attrKeyNames = new ArrayList<>();

    public IntegrationQueryDto() {
    }

    public IntegrationQueryDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCiTypeId() {
        return ciTypeId;
    }

    public void setCiTypeId(Integer ciTypeId) {
        this.ciTypeId = ciTypeId;
    }

    public List<Integer> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<Integer> attrs) {
        this.attrs = attrs;
    }

    public List<String> getAttrAliases() {
        return attrAliases;
    }

    public void setAttrAliases(List<String> attrAliases) {
        this.attrAliases = attrAliases;
    }

    public Relationship getParentRs() {
        return parentRs;
    }

    public void setParentRs(Relationship parentRs) {
        this.parentRs = parentRs;
    }

    public List<IntegrationQueryDto> getChildren() {
        return children;
    }

    public void setChildren(List<IntegrationQueryDto> children) {
        this.children = children;
    }

    public List<String> getAggKeyNames() {
        return aggKeyNames;
    }

    public void setAggKeyNames(List<String> aggKeyNames) {
        this.aggKeyNames = aggKeyNames;
    }

    public List<String> getAttrKeyNames() {
        return attrKeyNames;
    }

    public void setAttrKeyNames(List<String> attrKeyNames) {
        this.attrKeyNames = attrKeyNames;
    }


    @Override
    public String toString() {
        return "IntegrationQueryDto [name=" + name + ", ciTypeId=" + ciTypeId + ", attrs=" + attrs + ", attrAliases="
                + attrAliases + ", parentRs=" + parentRs + ", children=" + children + ", aggKeyNames=" + aggKeyNames
                + ", attrKeyNames=" + attrKeyNames + "]";
    }

}
