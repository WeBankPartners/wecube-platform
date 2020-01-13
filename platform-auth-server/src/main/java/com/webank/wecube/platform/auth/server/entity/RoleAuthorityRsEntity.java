package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_ROLE_AUTHORITY")
public class RoleAuthorityRsEntity extends BaseStatusFeaturedEntity {

    private String roleId;
    
    private String roleName;
    
    private String authorityId;
    
    private String authorityName;


	@ManyToOne
	@JoinColumn(name = "AUTHORITY_ID")
	private SysAuthorityEntity authority;

	@ManyToOne
	@JoinColumn(name = "ROLE_ID")
	private SysRoleEntity role;

	public RoleAuthorityRsEntity() {
	}

	public RoleAuthorityRsEntity(SysAuthorityEntity authority, SysRoleEntity role) {
		this.setAuthority(authority);
		this.setRole(role);
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
