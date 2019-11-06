package com.webank.wecube.platform.core.entity.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RU_TASK_NODE_EXEC_PARAM")
public class TaskNodeExecParamEntity extends BaseTraceableEntity {
    public static final String PARAM_TYPE_REQUEST = "REQ";
    public static final String PARAM_TYPE_RESPONSE = "RESP";

    public static final String PARAM_DATA_TYPE_INT = "int";
    public static final String PARAM_DATA_TYPE_STRING = "string";
    public static final String PARAM_DATA_TYPE_TIMESTAMP = "timestamp";

    public static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmssSSSZ";

    @Id
    @Column(name = "ID")
    @GeneratedValue
    private Integer id;

    @Column(name = "REQ_ID")
    private String requestId;

    @Column(name = "OBJ_ID")
    private String objectId; // TODO

    @Column(name = "PARAM_TYPE")
    private String paramType;

    @Column(name = "PARAM_NAME")
    private String paramName;

    @Column(name = "PARAM_DATA_TYPE")
    private String paramDataType; // int,string,boolean

    @Column(name = "PARAM_DATA_VALUE")
    private String paramDataValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
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

    public String getParamDataType() {
        return paramDataType;
    }

    public void setParamDataType(String paramDataType) {
        this.paramDataType = paramDataType;
    }

    public String getParamDataValue() {
        return paramDataValue;
    }

    public void setParamDataValue(String paramDataValue) {
        this.paramDataValue = paramDataValue;
    }

}
