package com.webank.wecube.platform.core.support.authserver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wecube.core.authserver")
public class AuthServerRestClientProperties {
    private String host = "localhost";
    private String httpSchema = "http";
    private int port = 9090;

    private String pathRegisterLocalUser = "/auth/v1/users";
    private String pathRetrieveAllUserAccounts = "/auth/v1/users";
    private String pathDeleteUserAccountByUserId = "/auth/v1/users/{user-id}";
    private String pathRetrieveGrantedRolesByUsername = "/auth/v1/users/{username}/roles";
    private String pathRetrieveRoleById = "/auth/v1/roles/{role-id}";
    private String pathRegisterLocalRole = "/auth/v1/roles";
    private String pathRetrieveAllRoles = "/auth/v1/roles";
    private String pathDeleteLocalRoleByRoleId = "/auth/v1/roles/{role-id}";
    private String pathRetrieveAllUsersBelongsToRoleId = "/auth/v1/roles/{role-id}/users";
    private String pathConfigureUserRolesById = "/auth/v1/roles/{role-id}/users";
    private String pathRevokeUserRolesById = "/auth/v1/roles/{role-id}/users/revoke";
    private String pathRevokeAuthoritiesFromRole = "/auth/v1/roles/{role-id}/authorities/revoke";
    private String pathConfigureRoleAuthorities = "/auth/v1/roles/{role-id}/authorities";
    private String pathConfigureRoleAuthoritiesWithRoleName = "/auth/v1/roles/authorities-grant";
    private String pathRevokeRoleAuthoritiesWithRoleName = "/auth/v1/roles/authorities-revocation";
    
    private String pathHealthCheck = "/auth/v1/health-check";


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHttpSchema() {
        return httpSchema;
    }

    public void setHttpSchema(String httpSchema) {
        this.httpSchema = httpSchema;
    }

    public String getPathRegisterLocalUser() {
        return pathRegisterLocalUser;
    }

    public void setPathRegisterLocalUser(String pathRegisterLocalUser) {
        this.pathRegisterLocalUser = pathRegisterLocalUser;
    }

    public String getPathRetrieveAllUserAccounts() {
        return pathRetrieveAllUserAccounts;
    }

    public void setPathRetrieveAllUserAccounts(String pathRetrieveAllUserAccounts) {
        this.pathRetrieveAllUserAccounts = pathRetrieveAllUserAccounts;
    }

    public String getPathDeleteUserAccountByUserId() {
        return pathDeleteUserAccountByUserId;
    }

    public void setPathDeleteUserAccountByUserId(String pathDeleteUserAccountByUserId) {
        this.pathDeleteUserAccountByUserId = pathDeleteUserAccountByUserId;
    }

    public String getPathRetrieveGrantedRolesByUsername() {
        return pathRetrieveGrantedRolesByUsername;
    }

    public void setPathRetrieveGrantedRolesByUsername(String pathRetrieveGrantedRolesByUsername) {
        this.pathRetrieveGrantedRolesByUsername = pathRetrieveGrantedRolesByUsername;
    }

    public String getPathRetrieveRoleById() {
        return pathRetrieveRoleById;
    }

    public void setPathRetrieveRoleById(String pathRetrieveRoleById) {
        this.pathRetrieveRoleById = pathRetrieveRoleById;
    }

    public String getPathRegisterLocalRole() {
        return pathRegisterLocalRole;
    }

    public void setPathRegisterLocalRole(String pathRegisterLocalRole) {
        this.pathRegisterLocalRole = pathRegisterLocalRole;
    }

    public String getPathRetrieveAllRoles() {
        return pathRetrieveAllRoles;
    }

    public void setPathRetrieveAllRoles(String pathRetrieveAllRoles) {
        this.pathRetrieveAllRoles = pathRetrieveAllRoles;
    }

    public String getPathDeleteLocalRoleByRoleId() {
        return pathDeleteLocalRoleByRoleId;
    }

    public void setPathDeleteLocalRoleByRoleId(String pathDeleteLocalRoleByRoleId) {
        this.pathDeleteLocalRoleByRoleId = pathDeleteLocalRoleByRoleId;
    }

    public String getPathRetrieveAllUsersBelongsToRoleId() {
        return pathRetrieveAllUsersBelongsToRoleId;
    }

    public void setPathRetrieveAllUsersBelongsToRoleId(String pathRetrieveAllUsersBelongsToRoleId) {
        this.pathRetrieveAllUsersBelongsToRoleId = pathRetrieveAllUsersBelongsToRoleId;
    }

    public String getPathConfigureUserRolesById() {
        return pathConfigureUserRolesById;
    }

    public void setPathConfigureUserRolesById(String pathConfigureUserRolesById) {
        this.pathConfigureUserRolesById = pathConfigureUserRolesById;
    }

    public String getPathRevokeUserRolesById() {
        return pathRevokeUserRolesById;
    }

    public void setPathRevokeUserRolesById(String pathRevokeUserRolesById) {
        this.pathRevokeUserRolesById = pathRevokeUserRolesById;
    }

    public String getPathRevokeAuthoritiesFromRole() {
        return pathRevokeAuthoritiesFromRole;
    }

    public void setPathRevokeAuthoritiesFromRole(String pathRevokeAuthoritiesFromRole) {
        this.pathRevokeAuthoritiesFromRole = pathRevokeAuthoritiesFromRole;
    }

    public String getPathConfigureRoleAuthorities() {
        return pathConfigureRoleAuthorities;
    }

    public void setPathConfigureRoleAuthorities(String pathConfigureRoleAuthorities) {
        this.pathConfigureRoleAuthorities = pathConfigureRoleAuthorities;
    }

    public String getPathConfigureRoleAuthoritiesWithRoleName() {
        return pathConfigureRoleAuthoritiesWithRoleName;
    }

    public void setPathConfigureRoleAuthoritiesWithRoleName(String pathConfigureRoleAuthoritiesWithRoleName) {
        this.pathConfigureRoleAuthoritiesWithRoleName = pathConfigureRoleAuthoritiesWithRoleName;
    }

    public String getPathRevokeRoleAuthoritiesWithRoleName() {
        return pathRevokeRoleAuthoritiesWithRoleName;
    }

    public void setPathRevokeRoleAuthoritiesWithRoleName(String pathRevokeRoleAuthoritiesWithRoleName) {
        this.pathRevokeRoleAuthoritiesWithRoleName = pathRevokeRoleAuthoritiesWithRoleName;
    }

	public String getPathHealthCheck() {
		return pathHealthCheck;
	}

	public void setPathHealthCheck(String pathHealthCheck) {
		this.pathHealthCheck = pathHealthCheck;
	}
}
