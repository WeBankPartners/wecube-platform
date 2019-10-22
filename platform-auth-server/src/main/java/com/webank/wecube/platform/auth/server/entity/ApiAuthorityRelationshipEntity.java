package com.webank.wecube.platform.auth.server.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AUTH_SYS_API_AUTHORITY")
public class ApiAuthorityRelationshipEntity extends AbstractTraceableEntity {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "API_ID")
	private SysApiEntity api;

	@ManyToOne
	@JoinColumn(name = "AUTHORITY_ID")
	private SysAuthorityEntity authority;

	public ApiAuthorityRelationshipEntity() {
	}

	public ApiAuthorityRelationshipEntity(SysApiEntity api, SysAuthorityEntity authority) {
		this.setApi(api);
		this.setAuthority(authority);
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

	public SysAuthorityEntity getAuthority() {
		return authority;
	}

	public void setAuthority(SysAuthorityEntity authority) {
		this.authority = authority;
	}

}
