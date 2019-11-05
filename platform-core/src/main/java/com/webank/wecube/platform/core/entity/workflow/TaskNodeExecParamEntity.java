package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RU_TASK_NODE_EXEC_PARAM")
public class TaskNodeExecParamEntity extends BaseTraceableEntity {
    public static final String PARAM_TYPE_INPUT = "IN";
    public static final String PARAM_TYPE_OUTPUT = "OUT";

    public static final String PARAM_DATA_TYPE_INT = "int";
    public static final String PARAM_DATA_TYPE_STRING = "string";
    public static final String PARAM_DATA_TYPE_TIMESTAMP = "timestamp";

    public static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmssSSSZ";

    @Id
    @Column(name = "ID")
    @GeneratedValue
    private Integer id;

    @Column(name = "PROC_INST_ID")
    private Integer procInstId;

    @Column(name = "TASK_NODE_INST_ID")
    private Integer taskNodeInstId;

    @Column(name = "ORDERED_NO")
    private Integer orderedNo;

    @Column(name = "PARAM_TYPE")
    private String paramType;

    @Column(name = "PARAM_NAME")
    private String paramName;

    @Column(name = "DATA_TYPE")
    private String dataType; // int,string,boolean

    @Column(name = "DATA_VALUE")
    private String dataValue;

    @Column(name = "OBJECT_ID")
    private String objectId; // TODO

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(Integer procInstId) {
        this.procInstId = procInstId;
    }

    public Integer getTaskNodeInstId() {
        return taskNodeInstId;
    }

    public void setTaskNodeInstId(Integer taskNodeInstId) {
        this.taskNodeInstId = taskNodeInstId;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public Integer getOrderedNo() {
        return orderedNo;
    }

    public void setOrderedNo(Integer orderedNo) {
        this.orderedNo = orderedNo;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
