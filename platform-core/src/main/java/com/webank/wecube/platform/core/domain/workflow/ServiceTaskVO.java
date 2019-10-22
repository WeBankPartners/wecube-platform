package com.webank.wecube.platform.core.domain.workflow;

public class ServiceTaskVO {
	private String id;
	private String name;
	private String serviceCode;
	
	private String ciLocateExpression;

	public ServiceTaskVO(String id, String name, String serviceCode) {
		super();
		this.id = id;
		this.name = name;
		this.serviceCode = serviceCode;
	}

	public ServiceTaskVO() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getCiLocateExpression() {
		return ciLocateExpression;
	}

	public void setCiLocateExpression(String ciLocateExpression) {
		this.ciLocateExpression = ciLocateExpression;
	}
}
