package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_USER")
public class SysUserEntity extends AbstractTraceableEntity {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@Column(name = "USERNAME")
	private String username;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "IS_ACTIVE")
	private Boolean active;

	@Column(name = "IS_BLOCKED")
	private Boolean blocked;

//	@ManyToMany(fetch = FetchType.EAGER)
//	@JoinTable(name = "AUTH_SYS_USER_ROLE", joinColumns = { @JoinColumn(name = "USER_ID") }, inverseJoinColumns = {
//			@JoinColumn(name = "ROLE_ID") })
//	private List<SysRoleEntity> roles = new ArrayList<SysRoleEntity>();

	public SysUserEntity() {
	}

	public SysUserEntity(String username, String password, Boolean active) {
		this.setUsername(username);
		this.setPassword(password);
		this.setActive(active);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

//	public List<SysRoleEntity> getRoles() {
//		return roles;
//	}
//
//	public void setRoles(List<SysRoleEntity> roles) {
//		this.roles = roles;
//	}

}
