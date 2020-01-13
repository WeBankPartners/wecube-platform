package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_SUB_SYSTEM")
public class SysSubSystemEntity extends BaseUUIDFeaturedEntity {

	@Column(name = "NAME")
	private String name;

	@Column(name = "SYSTEM_CODE")
	private String systemCode;

	@Column(name = "API_KEY", length = 500)
	private String apiKey;

	@Column(name = "PUB_API_KEY", length = 500)
	private String pubApiKey;

	@Column(name = "IS_ACTIVE")
	private Boolean active;

	@Column(name = "IS_BLOCKED")
	private Boolean blocked;

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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getBlocked() {
		return blocked;
	}

	public void setBlocked(Boolean blocked) {
		this.blocked = blocked;
	}

}
