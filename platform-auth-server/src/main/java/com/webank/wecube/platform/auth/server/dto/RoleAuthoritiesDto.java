package com.webank.wecube.platform.auth.server.dto;

import java.util.ArrayList;
import java.util.List;

public class RoleAuthoritiesDto {

	private String roleId;
	private String roleName;

	private List<SimpleAuthorityDto> authorities = new ArrayList<>();

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public List<SimpleAuthorityDto> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<SimpleAuthorityDto> authorities) {
		this.authorities = authorities;
	}

	public RoleAuthoritiesDto addAuthorities(SimpleAuthorityDto... authorities) {
		for (SimpleAuthorityDto d : authorities) {
			this.authorities.add(d);
		}
		
		return this;
	}
}
