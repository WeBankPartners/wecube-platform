package com.webank.wecube.platform.auth.server.dto;

import java.util.List;

public class SetUserRoleRelationshipDto {
	public abstract class UserRole {
		private Long userId;
		private Long roleId;
		public Long getUserId() {
			return userId;
		}
		public void setUserId(Long userId) {
			this.userId = userId;
		}
		public Long getRoleId() {
			return roleId;
		}
		public void setRoleId(Long roleId) {
			this.roleId = roleId;
		}
	}

	private List<UserRole> relationships;
	

	public List<UserRole> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<UserRole> relationships) {
		this.relationships = relationships;
	}
}
