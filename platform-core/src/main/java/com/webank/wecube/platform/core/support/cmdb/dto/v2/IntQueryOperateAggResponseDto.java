package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import java.util.List;

public class IntQueryOperateAggResponseDto {
	private String queryName;
	private Integer queryId;
	private List<AggBranch> branchs;

	public static class AggBranch{
		private String branchId;
		private String alias;

		public String getBranchId() {
			return branchId;
		}
		public void setBranchId(String branchId) {
			this.branchId = branchId;
		}
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}

	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public Integer getQueryId() {
		return queryId;
	}

	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}

	public List<AggBranch> getBranchs() {
		return branchs;
	}

	public void setBranchs(List<AggBranch> branchs) {
		this.branchs = branchs;
	}

}
