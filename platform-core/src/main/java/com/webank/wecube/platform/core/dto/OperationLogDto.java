package com.webank.wecube.platform.core.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.webank.wecube.platform.core.domain.OperationLog;

public class OperationLogDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String operator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date operateTime;
    private String category;
    private String content;
    private String operation;
    private String result;
    private String resultMessage;

    public static OperationLogDto fromDomain(OperationLog domain) {
        OperationLogDto dto = new OperationLogDto();

        dto.setId(String.valueOf(domain.getId()));
        dto.setOperator(domain.getOperator());
        dto.setOperateTime(domain.getOperateTime());
        dto.setCategory(domain.getCategory());
        dto.setContent(domain.getContent());
        dto.setOperation(domain.getOperation());
        dto.setResult(domain.getResult());
        dto.setResultMessage(domain.getResultMessage());
        return dto;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getOperateTime() {
        return this.operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}