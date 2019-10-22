package com.webank.wecube.platform.core.domain.workflow;

import java.util.ArrayList;
import java.util.List;

public class ProcessTransactionVO {
    private Integer id;
    private String name;
    private String aliasName;
    private String operator;
    private String operatorGroup;
    private String status;

    private List<ProcessTaskVO> tasks = new ArrayList<ProcessTaskVO>();

    private AttachVO attach;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperatorGroup() {
        return operatorGroup;
    }

    public void setOperatorGroup(String operatorGroup) {
        this.operatorGroup = operatorGroup;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ProcessTaskVO> getTasks() {
        return tasks;
    }

    public void setTasks(List<ProcessTaskVO> tasks) {
        this.tasks = tasks;
    }

    public void addTask(ProcessTaskVO task) {
        this.tasks.add(task);
    }

    public AttachVO getAttach() {
        return attach;
    }

    public void setAttach(AttachVO attach) {
        this.attach = attach;
    }

}
