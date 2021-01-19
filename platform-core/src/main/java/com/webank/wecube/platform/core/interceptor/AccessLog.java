package com.webank.wecube.platform.core.interceptor;

public class AccessLog {
    private String id;
    private String remoteAddr;
    private String username;
    private String httpMethod;
    private String path;
    private long startTime;
    private long endTime;
    private String requestData;
    private String responseStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public static String compactAccessLog(AccessLog log) {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[id=");
        builder.append(id);
        builder.append(", remoteAddr=");
        builder.append(remoteAddr);
        builder.append(", username=");
        builder.append(username);
        builder.append(", httpMethod=");
        builder.append(httpMethod);
        builder.append(", path=");
        builder.append(path);
        builder.append(", startTime=");
        builder.append(startTime);
        builder.append(", endTime=");
        builder.append(endTime);
        builder.append(", requestData=");
        builder.append(requestData);
        builder.append(", responseStatus=");
        builder.append(responseStatus);
        builder.append("]");
        return builder.toString();
    }

}
