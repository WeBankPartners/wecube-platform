package com.webank.wecube.platform.auth.server.dto;

public class CreateSubsystemDto {
	private String name;
	private String systemCode;
	private String apiKey;
	private String pubApiKey;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getPubApiKey() {
		return pubApiKey;
	}

	public void setPubApiKey(String pubApiKey) {
		this.pubApiKey = pubApiKey;
	}
}
