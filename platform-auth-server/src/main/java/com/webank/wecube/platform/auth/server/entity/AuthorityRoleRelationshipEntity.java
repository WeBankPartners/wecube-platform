package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_AUTHORITY_ROLE")
public class AuthorityRoleRelationshipEntity extends AbstractTraceableEntity {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "AUTHORITY_ID")
	private SysAuthorityEntity authority;

	@ManyToOne
	@JoinColumn(name = "ROLE_ID")
	private SysRoleEntity role;

	public AuthorityRoleRelationshipEntity() {
	}

	public AuthorityRoleRelationshipEntity(SysAuthorityEntity authority, SysRoleEntity role) {
		this.setAuthority(authority);
		this.setRole(role);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SysAuthorityEntity getAuthority() {
		return authority;
	}

	public void setAuthority(SysAuthorityEntity authority) {
		this.authority = authority;
	}

	public SysRoleEntity getRole() {
		return role;
	}

	public void setRole(SysRoleEntity roleId) {
		this.role = roleId;
	}

}
