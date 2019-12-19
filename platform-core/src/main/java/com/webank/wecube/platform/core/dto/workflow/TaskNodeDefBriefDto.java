package com.webank.wecube.platform.core.dto.workflow;

public class TaskNodeDefBriefDto {
    
    private String nodeId;
    private String nodeName;
    private String nodeType;
    
    private String nodeDefId;
    private String procDefId;

    private String serviceId;
    private String serviceName;
    public String getNodeId() {
        return nodeId;
    }
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    public String getNodeType() {
        return nodeType;
    }
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    public String getNodeDefId() {
        return nodeDefId;
    }
    public void setNodeDefId(String nodeDefId) {
        this.nodeDefId = nodeDefId;
    }
    public String getProcDefId() {
        return procDefId;
    }
    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }
    public String getServiceId() {
        return serviceId;
    }
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    

}
