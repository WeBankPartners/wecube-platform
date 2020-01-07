package com.webank.wecube.platform.auth.server.dto;

import javax.validation.constraints.Email;

public class CreateRoleDto {
	private String name;
	private String displayName;
	@Email(message = "Please provide a valid E-mail address.")
	private String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
