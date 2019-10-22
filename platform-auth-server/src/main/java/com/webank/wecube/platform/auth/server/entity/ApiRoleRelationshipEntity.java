package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_API_ROLE")
public class ApiRoleRelationshipEntity extends AbstractTraceableEntity {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "API_ID")
	private SysApiEntity api;
	@ManyToOne
	@JoinColumn(name = "ROLE_ID")
	private SysRoleEntity role;

	public ApiRoleRelationshipEntity() {
	}

	public ApiRoleRelationshipEntity(SysApiEntity api, SysRoleEntity role) {
		this.setApi(api);
		this.setRole(role);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SysApiEntity getApi() {
		return api;
	}

	public void setApi(SysApiEntity api) {
		this.api = api;
	}

	public SysRoleEntity getRole() {
		return role;
	}

	public void setRole(SysRoleEntity role) {
		this.role = role;
	}

}
