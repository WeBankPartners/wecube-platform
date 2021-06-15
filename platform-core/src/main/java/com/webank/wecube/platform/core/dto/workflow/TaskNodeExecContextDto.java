package com.webank.wecube.platform.core.dto.workflow;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskNodeExecContextDto {
    private String nodeName;
    private String nodeId;
    private String nodeDefId;
    private Integer nodeInstId;
    private String nodeType;
    
    private String nodeExpression;
    private String pluginInfo;

    private String requestId;
    private String errorCode;
    private String errorMessage;

    private List<RequestObjectDto> requestObjects = new ArrayList<>();

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeDefId() {
        return nodeDefId;
    }

    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }

    public Integer getNodeInstId() {
        return nodeInstId;
    }

    public void setNodeInstId(Integer nodeInstId) {
        this.nodeInstId = nodeInstId;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<RequestObjectDto> getRequestObjects() {
        return requestObjects;
    }

    public void setRequestObjects(List<RequestObjectDto> requestObjects) {
        this.requestObjects = requestObjects;
    }

    public void addRequestObjects(RequestObjectDto... dtos) {
        for (RequestObjectDto dto : dtos) {
            if (dto != null) {
                this.requestObjects.add(dto);
            }
        }
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMsg) {
        this.errorMessage = errorMsg;
    }

    public String getNodeExpression() {
        return nodeExpression;
    }

    public void setNodeExpression(String nodeExpression) {
        this.nodeExpression = nodeExpression;
    }

    public String getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginInfo(String pluginInfo) {
        this.pluginInfo = pluginInfo;
    }
    
    

}
