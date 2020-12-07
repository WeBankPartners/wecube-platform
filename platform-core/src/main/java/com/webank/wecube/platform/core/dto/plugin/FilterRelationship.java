package com.webank.wecube.platform.core.dto.plugin;

public enum FilterRelationship {
	NONE("none"), AND("and"), OR("or");

	private String code;

	private FilterRelationship(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	static public FilterRelationship fromCode(String code) {
		for (FilterRelationship filterRs : values()) {
			if (NONE.equals(filterRs))
				continue;

			if (filterRs.getCode().equals(code)) {
				return filterRs;
			}
		}
		return NONE;
	}
	
}
