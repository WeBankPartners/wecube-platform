package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_USER_ROLE")
public class UserRoleRelationshipEntity extends AbstractTraceableEntity {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "USER_ID")
	private SysUserEntity user;
	@ManyToOne
	@JoinColumn(name = "ROLE_ID")
	private SysRoleEntity role;

	public UserRoleRelationshipEntity() {
	}

	public UserRoleRelationshipEntity(SysUserEntity user, SysRoleEntity role) {
		this.setUser(user);
		this.setRole(role);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SysUserEntity getUser() {
		return user;
	}

	public void setUser(SysUserEntity user) {
		this.user = user;
	}

	public SysRoleEntity getRole() {
		return role;
	}

	public void setRole(SysRoleEntity role) {
		this.role = role;
	}

}
