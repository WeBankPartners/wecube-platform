package com.webank.wecube.platform.core.domain;


import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.*;

//@Entity
//@Table(name = "operation_log")
public class OperationLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String operator;
    private Timestamp operateTime;
    private String category;
    private String content;
    private String operation;
    private String result;
    private String resultMessage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "operator")
    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Column(name = "operate_time")
    public Timestamp getOperateTime() {
        return this.operateTime;
    }

    public void setOperateTime(Timestamp operateTime) {
        this.operateTime = operateTime;
    }

    @Lob
    @Column(name = "content")
    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(name = "operation")
    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Column(name = "result")
    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Column(name = "resultMessage")
    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    @Column(name = "category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}