package com.webank.wecube.platform.core.entity.workflow;

public class ProcInstExecContextEntity {
    public static final String REQ_DIR_REQUEST = "request";
    public static final String REQ_DIR_RESPONSE = "response";
    
    public static final String CTX_TYPE_PROCESS = "process";
    public static final String CTX_TYPE_NODE = "taskNode";
    public static final String CTX_TYPE_REQUEST = "request";
    
    public static final String CTX_DATA_FORMAT_JSON = "JSON";
    public static final String CTX_DATA_FORMAT_STRING = "string";

    private String id;
    private String procDefId;
    private Integer procInstId;
    private String nodeDefId;
    private Integer nodeInstId;
    private String reqId;
    private String reqDir;//request,response

    private String ctxType;// process,taskNode,request
    private String ctxDataFormat;// JSON,string
    private String ctxData;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCtxType() {
        return ctxType;
    }

    public void setCtxType(String ctxType) {
        this.ctxType = ctxType;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
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

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getCtxData() {
        return ctxData;
    }

    public void setCtxData(String ctxData) {
        this.ctxData = ctxData;
    }

    public String getCtxDataFormat() {
        return ctxDataFormat;
    }

    public void setCtxDataFormat(String ctxDataFormat) {
        this.ctxDataFormat = ctxDataFormat;
    }

    public String getReqDir() {
        return reqDir;
    }

    public void setReqDir(String reqDir) {
        this.reqDir = reqDir;
    }

    
}
