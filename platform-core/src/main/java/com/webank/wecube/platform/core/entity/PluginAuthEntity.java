//package com.webank.wecube.platform.core.entity;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.Table;
//
//import org.hibernate.annotations.GenericGenerator;
//
//@Entity
//@Table(name = "PLUGIN_CONFIG_ROLES")
//public class PluginAuthEntity extends BaseTraceableEntity {
//	public static final String PERM_TYPE_MGMT = "MGMT";
//	public static final String PERM_TYPE_USE = "USE";
//	@Id
//	@Column(name = "ID")
//	@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
//	@GeneratedValue(generator = "jpa-uuid")
//	private String id;
//	@Column(name = "PLUGIN_CFG_ID")
//	private String pluginConfigId;
//	@Column(name = "ROLE_ID")
//	private String roleId;
//	@Column(name = "ROLE_NAME")
//	private String roleName;
//	@Column(name = "PERM_TYPE")
//	private String permissionType;
//	@Column(name = "IS_ACTIVE")
//	private boolean active;
//
//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}
//
//	public String getPluginConfigId() {
//		return pluginConfigId;
//	}
//
//	public void setPluginConfigId(String pluginConfigId) {
//		this.pluginConfigId = pluginConfigId;
//	}
//
//	public String getRoleId() {
//		return roleId;
//	}
//
//	public void setRoleId(String roleId) {
//		this.roleId = roleId;
//	}
//
//	public String getRoleName() {
//		return roleName;
//	}
//
//	public void setRoleName(String roleName) {
//		this.roleName = roleName;
//	}
//
//	public String getPermissionType() {
//		return permissionType;
//	}
//
//	public void setPermissionType(String permissionType) {
//		this.permissionType = permissionType;
//	}
//
//	public boolean isActive() {
//		return active;
//	}
//
//	public void setActive(boolean active) {
//		this.active = active;
//	}
//
//}
