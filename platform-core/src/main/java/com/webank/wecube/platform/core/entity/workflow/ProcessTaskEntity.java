package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="core_ru_process_task")
public class ProcessTaskEntity {
	@Id
	@Column(name = "ID")
	@GeneratedValue
	private Integer id;

	@Column(name = "OPERATOR")
	private String operator;

	@Column(name = "OPERATOR_GROUP")
	private String operatorGroup;

	@Column(name = "DEF_ID")
	private String processDefinitionId;

	@Column(name = "DEF_KEY")
	private String processDefinitionKey;

	@Column(name = "DEF_VER")
	private Integer processDefinitionVersion;

	@Column(name = "INST_ID")
	private String processInstanceId;

	@Column(name = "INST_KEY")
	private String processInstanceKey;

	@Column(name = "CI_TYPE_ID")
	private Integer rootCiTypeId;

	@Column(name = "CI_DATA_ID")
	private String rootCiDataId;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "START_TIME")
	private Date startTime;

	@Column(name = "END_TIME")
	private Date endTime;

	@Column(name = "CREATE_TIME")
	private Date createTime;

	@Column(name = "CREATE_BY")
	private String createBy;

	@Column(name = "UPDATE_TIME")
	private Date updateTime;

	@Column(name = "UPDATE_BY")
	private String updateBy;

	@ManyToOne
	@JoinColumn(name = "TRANSACTION_ID")
	private ProcessTransactionEntity transaction;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public Integer getProcessDefinitionVersion() {
		return processDefinitionVersion;
	}

	public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
		this.processDefinitionVersion = processDefinitionVersion;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getProcessInstanceKey() {
		return processInstanceKey;
	}

	public void setProcessInstanceKey(String processInstanceKey) {
		this.processInstanceKey = processInstanceKey;
	}

	public Integer getRootCiTypeId() {
		return rootCiTypeId;
	}

	public void setRootCiTypeId(Integer rootCiTypeId) {
		this.rootCiTypeId = rootCiTypeId;
	}

	public String getRootCiDataId() {
		return rootCiDataId;
	}

	public void setRootCiDataId(String rootCiDataId) {
		this.rootCiDataId = rootCiDataId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
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

	public ProcessTransactionEntity getTransaction() {
		return transaction;
	}

	public void setTransaction(ProcessTransactionEntity transaction) {
		this.transaction = transaction;
	}
	
}
