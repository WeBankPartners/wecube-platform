package com.webank.wecube.platform.core.support.itsdanger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItsDangerCheckReqDto {

    private String operator;
    private String serviceName;
    private String servicePath;
    private String entityType;
    private List<ItsDangerInstanceInfoDto> entityInstances = new ArrayList<>();
    private List<Map<String, Object>> inputParams = new ArrayList<>();

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public List<ItsDangerInstanceInfoDto> getEntityInstances() {
        return entityInstances;
    }

    public void setEntityInstances(List<ItsDangerInstanceInfoDto> entityInstances) {
        this.entityInstances = entityInstances;
    }

    public List<Map<String, Object>> getInputParams() {
        return inputParams;
    }

    public void setInputParams(List<Map<String, Object>> inputParams) {
        this.inputParams = inputParams;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ItsDangerCheckReqDto [operator=");
        builder.append(operator);
        builder.append(", serviceName=");
        builder.append(serviceName);
        builder.append(", servicePath=");
        builder.append(servicePath);
        builder.append(", entityType=");
        builder.append(entityType);
        builder.append(", entityInstances=");
        builder.append(entityInstances);
        builder.append(", inputParams=");
        builder.append(inputParams);
        builder.append("]");
        return builder.toString();
    }

}
