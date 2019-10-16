package com.webank.wecube.platform.core.entity.workflow;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="core_ru_process_transaction")
public class ProcessTransactionEntity {
	
	@Id
	@Column(name="ID")
    @GeneratedValue
    private Integer id;
	
	@Column(name="NAME")
	private String name;
	
	@Column(name="ALIAS_NAME")
	private String aliasName;
	
	@Column(name="OPERATOR")
	private String operator;
	
	@Column(name="OPERATOR_GROUP")
	private String operatorGroup;
	
	@Column(name="STATUS")
	private String status;
	
	@Column(name="START_TIME")
	private Date startTime;
	
	@Column(name="END_TIME")
	private Date endTime;
	
	@Column(name="CREATE_TIME")
	private Date createTime;
	
	@Column(name="CREATE_BY")
	private String createBy;
	
	@Column(name="UPDATE_TIME")
	private Date updateTime;
	
	@Column(name="UPDATE_BY")
	private String updateBy;
	
	@OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProcessTaskEntity> tasks = new HashSet<ProcessTaskEntity>();
	
	@Column(name="ATTACH")
	private String attach;

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

	public Set<ProcessTaskEntity> getTasks() {
		return tasks;
	}

	public void setTasks(Set<ProcessTaskEntity> tasks) {
		this.tasks = tasks;
	}
	
	public void addTask(ProcessTaskEntity task) {
		if(this.getTasks() == null) {
			this.tasks = new HashSet<>();
		}
		
		this.tasks.add(task);
		
		task.setTransaction(this);
	}

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }
}
