package com.webank.wecube.core.domain.workflow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORE_RE_PROC_DEF")
public class ProcessDefinitionEntity {

	public static final int ACTIVE_VALUE = 1;
	public static final int INACTIVE_VALUE = 0;

	@Id
	@Column(name = "ID")
	private String id;
	@Column(name = "PROC_DEF_KEY")
	private String procDefKey;
	@Column(name = "PROC_NAME")
	private String procName;
	@Column(name = "VERSION")
	private Integer version;
	@Column(name = "BIND_CITYPE_ID")
	private Integer bindCiTypeId;
	@Column(name = "ACTIVE")
	private Integer active;

	@Column(name = "CREATE_TIME")
	private Date createTime;
	@Column(name = "CREATE_BY")
	private String createBy;
	@Column(name = "UPDATE_TIME")
	private Date updateTime;
	@Column(name = "UPDATE_BY")
	private String updateBy;

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

	public String getProcName() {
		return procName;
	}

	public void setProcName(String procName) {
		this.procName = procName;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getBindCiTypeId() {
		return bindCiTypeId;
	}

	public void setBindCiTypeId(Integer bindCiTypeId) {
		this.bindCiTypeId = bindCiTypeId;
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

	public boolean isActive() {
		return ((this.active != null) && (this.active == ACTIVE_VALUE));
	}

}
