package com.webank.wecube.platform.core.dto.workflow;

public class TaskNodeDefObjectBindInfoDto {

	private String nodeDefId;
	private String orderedNo;
	private String entityTypeId;
	private String entityDataId;

	private String bound;// "Y"-bound,default;"N"-unbound;

	public String getOrderedNo() {
		return orderedNo;
	}

	public void setOrderedNo(String orderedNo) {
		this.orderedNo = orderedNo;
	}

	public String getNodeDefId() {
		return nodeDefId;
	}

	public void setNodeDefId(String nodeDefId) {
		this.nodeDefId = nodeDefId;
	}

	public String getEntityTypeId() {
		return entityTypeId;
	}

	public void setEntityTypeId(String entityTypeId) {
		this.entityTypeId = entityTypeId;
	}

	public String getEntityDataId() {
		return entityDataId;
	}

	public void setEntityDataId(String entityDataId) {
		this.entityDataId = entityDataId;
	}

	public String getBound() {
		return bound;
	}

	public void setBound(String bound) {
		this.bound = bound;
	}

}
