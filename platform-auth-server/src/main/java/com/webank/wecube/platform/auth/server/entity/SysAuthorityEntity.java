package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_AUTHORITY")
public class SysAuthorityEntity extends BaseStatusFeaturedEntity {

	@Column(name = "CODE")
	private String code;

	@Column(name = "DISPLAY_NAME")
	private String displayName;

	@Column(name = "SYSTEM_ID")
	private Long systemId;

	@Column(name = "SYSTEM_CODE")
	private String systemCode;

	@Column(name = "SYSTEM_NAME")
	private String systemName;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

}
