package com.webank.wecube.platform.auth.server.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_AUTHORITY")
public class SysAuthorityEntity extends AbstractTraceableEntity {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

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

//	@ManyToMany(fetch = FetchType.EAGER)
//	@JoinTable(name = "AUTH_SYS_AUTHORITY_ROLE", joinColumns = {
//			@JoinColumn(name = "AUTHORITY_ID") }, inverseJoinColumns = { @JoinColumn(name = "ROLE_ID") })
//	private List<SysRoleEntity> boundRoles = new ArrayList<SysRoleEntity>();

	public SysAuthorityEntity() {
	}

	public SysAuthorityEntity(String code, String displayName, Long systemId, String systemCode, String systemName) {
		this.setCode(code);
		this.setDisplayName(displayName);
		this.setSystemId(systemId);
		this.setSystemCode(systemCode);
		this.setSystemName(systemName);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

//	public List<SysRoleEntity> getBoundRoles() {
//		return boundRoles;
//	}
//
//	public void setBoundRoles(List<SysRoleEntity> boundRoles) {
//		this.boundRoles = boundRoles;
//	}

}
