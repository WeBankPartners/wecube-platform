package com.webank.wecube.core.domain.workflow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RE_PROC_TASK_SERVICE")
public class ProcessDefinitionTaskServiceEntity {
	public static final int ACTIVE_VALUE = 1;
	public static final int INACTIVE_VALUE = 0;
	
	public static final String TASK_NODE_TYPE_SERVICE_TASK = "serviceTask";
	public static final String TASK_NODE_TYPE_SUBPROCESS = "subprocess";

	@Id
	@Column(name="ID")
	private String id;
	@Column(name="PROC_DEF_KEY")
	private String procDefKey;
	@Column(name="VERSION")
	private Integer version;
	@Column(name="PROC_DEF_ID")
	private String procDefId;
	@Column(name="TASK_NODE_ID")
	private String taskNodeId;
	@Column(name="TASK_NODE_NAME")
	private String taksNodeName;

	@Column(name="BIND_SERVICE_ID")
	private String bindServiceId;
	@Column(name="BIND_SERVICE_NAME")
	private String bindServiceName;
	@Column(name="BIND_CI_ROUTINE_EXP")
	private String bindCiRoutineExp;
	@Column(name="BIND_CI_ROUTINE_RAW")
	private String bindCiRoutineRaw;
	@Column(name="DESCRIPTION")
	private String description;

	@Column(name="ACTIVE")
	private Integer active;

	@Column(name="CREATE_TIME")
	private Date createTime;
	@Column(name="CREATE_BY")
	private String createBy;
	@Column(name="UPDATE_TIME")
	private Date updateTime;
	@Column(name="UPDATE_BY")
	private String updateBy;
	
	@Column(name="TIMEOUT_EXPR")
	private String timeoutExpression;
	
	@Column(name="TASK_NODE_TYPE")
	private String taskNodeType;
	
	@Column(name="CORE_PROC_DEF_ID")
	private String coreProcDefId;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProcDefKey() {
		return procDefKey;
	}

	public void setProcDefKey(String procDefKey) {
		this.procDefKey = procDefKey;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getProcDefId() {
		return procDefId;
	}

	public void setProcDefId(String procDefId) {
		this.procDefId = procDefId;
	}

	public String getTaskNodeId() {
		return taskNodeId;
	}

	public void setTaskNodeId(String taskNodeId) {
		this.taskNodeId = taskNodeId;
	}

	public String getTaksNodeName() {
		return taksNodeName;
	}

	public void setTaksNodeName(String taksNodeName) {
		this.taksNodeName = taksNodeName;
	}

	public String getBindServiceId() {
		return bindServiceId;
	}

	public void setBindServiceId(String bindServiceId) {
		this.bindServiceId = bindServiceId;
	}

	public String getBindServiceName() {
		return bindServiceName;
	}

	public void setBindServiceName(String bindServiceName) {
		this.bindServiceName = bindServiceName;
	}

	public String getBindCiRoutineExp() {
		return bindCiRoutineExp;
	}

	public void setBindCiRoutineExp(String bindCiRoutineExp) {
		this.bindCiRoutineExp = bindCiRoutineExp;
	}

	public String getBindCiRoutineRaw() {
		return bindCiRoutineRaw;
	}

	public void setBindCiRoutineRaw(String bindCiRoutineRaw) {
		this.bindCiRoutineRaw = bindCiRoutineRaw;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return ((this.active != null ) && (this.active == 1));
	}

    public String getTimeoutExpression() {
        return timeoutExpression;
    }

    public void setTimeoutExpression(String timeoutExpression) {
        this.timeoutExpression = timeoutExpression;
    }

    public String getTaskNodeType() {
        return taskNodeType;
    }

    public void setTaskNodeType(String taskNodeType) {
        this.taskNodeType = taskNodeType;
    }

    public String getCoreProcDefId() {
        return coreProcDefId;
    }

    public void setCoreProcDefId(String coreProcDefId) {
        this.coreProcDefId = coreProcDefId;
    }
	
}
