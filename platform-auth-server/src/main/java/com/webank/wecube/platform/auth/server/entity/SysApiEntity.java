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
@Table(name = "AUTH_SYS_API")
public class SysApiEntity extends AbstractTraceableEntity {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "API_URL")
	private String apiUrl;

	@Column(name = "HTTP_METHOD")
	private String httpMethod;

	@Column(name = "SYSTEM_ID")
	private Long systemId;

	@Column(name = "SYSTEM_NAME")
	private String systemName;

	@Column(name = "SYSTEM_CODE")
	private String systemCode;

//	@ManyToMany(fetch = FetchType.LAZY)
//	@JoinTable(name = "AUTH_SYS_API_ROLE", joinColumns = { @JoinColumn(name = "API_ID") }, inverseJoinColumns = {
//			@JoinColumn(name = "ROLE_ID") })
//	private List<SysRoleEntity> boundRoles = new ArrayList<SysRoleEntity>();

//	@ManyToMany(fetch = FetchType.LAZY)
//	@JoinTable(name = "AUTH_SYS_API_AUTHORITY", joinColumns = { @JoinColumn(name = "API_ID") }, inverseJoinColumns = {
//			@JoinColumn(name = "AUTHORITY_ID") })
//	private List<SysAuthorityEntity> boundAuthorities = new ArrayList<SysAuthorityEntity>();

	public SysApiEntity() {
	}

	public SysApiEntity(String name, String apiUrl, String httpMethod, Long systemId, String systemName,
			String systemCode) {
		this.setName(name);
		this.setApiUrl(apiUrl);
		this.setHttpMethod(httpMethod);
		this.setSystemId(systemId);
		this.setSystemName(systemName);
		this.setSystemCode(systemCode);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public Long getSystemId() {
		return systemId;
	}

	public void setSystemId(Long systemId) {
		this.systemId = systemId;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getSystemCode() {
		return systemCode;
	}

	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}

//	public List<SysRoleEntity> getBoundRoles() {
//		return boundRoles;
//	}
//
//	public void setBoundRoles(List<SysRoleEntity> boundRoles) {
//		this.boundRoles = boundRoles;
//	}
//
//	public List<SysAuthorityEntity> getBoundAuthorities() {
//		return boundAuthorities;
//	}
//
//	public void setBoundAuthorities(List<SysAuthorityEntity> boundAuthorities) {
//		this.boundAuthorities = boundAuthorities;
//	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
}
