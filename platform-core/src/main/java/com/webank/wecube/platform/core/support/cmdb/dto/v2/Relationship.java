package com.webank.wecube.platform.core.support.cmdb.dto.v2;

public class Relationship{
	private Integer attrId;
	private boolean isReferedFromParent = true;
	
	public Relationship() {
	}
	
	public Relationship(Integer attrId, boolean isReferedFromParent) {
		this.attrId = attrId;
		this.isReferedFromParent = isReferedFromParent;
	}
	
	public Integer getAttrId() {
		return attrId;
	}
	public void setAttrId(Integer attrId) {
		this.attrId = attrId;
	}

	public boolean getIsReferedFromParent() {
		return isReferedFromParent;
	}

	public void setIsReferedFromParent(boolean isReferedFromParent) {
		this.isReferedFromParent = isReferedFromParent;
	}

	@Override
	public String toString() {
		return "Relationship [attrId=" + attrId + ", isReferedFromParent=" + isReferedFromParent + "]";
	}
}