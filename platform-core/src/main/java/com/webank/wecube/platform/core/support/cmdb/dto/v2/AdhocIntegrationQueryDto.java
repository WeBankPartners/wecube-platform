package com.webank.wecube.platform.core.support.cmdb.dto.v2;

public class AdhocIntegrationQueryDto {
	private IntegrationQueryDto criteria;
	private PaginationQuery queryRequest ;
	
	public IntegrationQueryDto getCriteria() {
		return criteria;
	}
	public void setCriteria(IntegrationQueryDto criteria) {
		this.criteria = criteria;
	}
	public PaginationQuery getQueryRequest() {
		return queryRequest;
	}
	public void setQueryRequest(PaginationQuery queryRequest) {
		this.queryRequest = queryRequest;
	}
	@Override
	public String toString() {
		return "AdhocIntegrationQueryDto [criteria=" + criteria + ", queryRequest=" + queryRequest + "]";
	}
	
	
}
