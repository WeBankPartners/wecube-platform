package com.webank.wecube.platform.core.support.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.parser.datamodel.antlr4.DataModelParser;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class DataModelExpressionDto {
    private String expression;
    private Stack<Set<String>> requestUrlStack = new Stack<>();
    private Stack<List<CommonResponseDto>> jsonResponseStack = new Stack<>();
    private List<Object> resultValue;

    // helper during processing
    @JsonIgnore
    private DataModelParser.LinkContext prevLink;
    @JsonIgnore
    private DataModelParser.Fwd_nodeContext fwdNode;
    @JsonIgnore
    private DataModelParser.FetchContext opFetch;
    @JsonIgnore
    private DataModelParser.ToContext opTo;
    @JsonIgnore
    private DataModelParser.EntityContext entity;
    @JsonIgnore
    private DataModelParser.ByContext opBy;
    @JsonIgnore
    private DataModelParser.Bwd_nodeContext bwdNode;

    // Tree node
    @JsonIgnore
    private TreeNode treeNode;

    // refTo constructor
    public DataModelExpressionDto(DataModelParser.LinkContext prevLink,
                                  DataModelParser.Fwd_nodeContext fwdNode,
                                  DataModelParser.FetchContext opFetch,
                                  DataModelParser.ToContext opTo,
                                  DataModelParser.EntityContext entity) {
        this.prevLink = prevLink;
        this.fwdNode = fwdNode;
        this.opFetch = opFetch;
        this.opTo = opTo;
        this.entity = entity;
    }

    // refBy constructor
    public DataModelExpressionDto(DataModelParser.LinkContext prevLink,
                                  DataModelParser.EntityContext entity,
                                  DataModelParser.ByContext opBy,
                                  DataModelParser.Bwd_nodeContext bwdNode) {
        this.prevLink = prevLink;
        this.entity = entity;
        this.opBy = opBy;
        this.bwdNode = bwdNode;
    }

    // route constructor with "prevLink fetch"
    public DataModelExpressionDto(DataModelParser.LinkContext prevLink, DataModelParser.FetchContext opFetch) {
        this.prevLink = prevLink;
        this.opFetch = opFetch;
    }

    // route constructor with "entity fetch"
    public DataModelExpressionDto(DataModelParser.EntityContext entity, DataModelParser.FetchContext opFetch) {
        this.entity = entity;
        this.opFetch = opFetch;
    }

    public DataModelExpressionDto(String expression, Stack<List<CommonResponseDto>> jsonResponseStack) {
        this.expression = expression;
        this.jsonResponseStack = jsonResponseStack;
    }

    public DataModelExpressionDto() {
    }

    public DataModelExpressionDto(String requestUrl, CommonResponseDto responseDto) {
        this.requestUrlStack = new Stack<>();
        this.jsonResponseStack = new Stack<>();
        this.requestUrlStack.add(Collections.singleton(requestUrl));
        this.jsonResponseStack.add(Collections.singletonList(responseDto));
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Stack<List<CommonResponseDto>> getJsonResponseStack() {
        return jsonResponseStack;
    }

    public void setJsonResponseStack(Stack<List<CommonResponseDto>> jsonResponseStack) {
        this.jsonResponseStack = jsonResponseStack;
    }

    public DataModelParser.Fwd_nodeContext getFwdNode() {
        return fwdNode;
    }

    public void setFwdNode(DataModelParser.Fwd_nodeContext fwdNode) {
        this.fwdNode = fwdNode;
    }

    public DataModelParser.ToContext getOpTo() {
        return opTo;
    }

    public void setOpTo(DataModelParser.ToContext opTo) {
        this.opTo = opTo;
    }

    public DataModelParser.EntityContext getEntity() {
        return entity;
    }

    public void setEntity(DataModelParser.EntityContext entity) {
        this.entity = entity;
    }

    public DataModelParser.ByContext getOpBy() {
        return opBy;
    }

    public void setOpBy(DataModelParser.ByContext opBy) {
        this.opBy = opBy;
    }

    public DataModelParser.Bwd_nodeContext getBwdNode() {
        return bwdNode;
    }

    public void setBwdNode(DataModelParser.Bwd_nodeContext bwdNode) {
        this.bwdNode = bwdNode;
    }

    public DataModelParser.FetchContext getOpFetch() {
        return opFetch;
    }

    public void setOpFetch(DataModelParser.FetchContext opFetch) {
        this.opFetch = opFetch;
    }

    public DataModelParser.LinkContext getPrevLink() {
        return prevLink;
    }

    public void setPrevLink(DataModelParser.LinkContext prevLink) {
        this.prevLink = prevLink;
    }

    public List<Object> getResultValue() {
        return resultValue;
    }

    public void setResultValue(List<Object> resultValue) {
        this.resultValue = resultValue;
    }

    public Stack<Set<String>> getRequestUrlStack() {
        return requestUrlStack;
    }

    public void setRequestUrlStack(Stack<Set<String>> requestUrlStack) {
        this.requestUrlStack = requestUrlStack;
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(TreeNode treeNode) {
        this.treeNode = treeNode;
    }
}
