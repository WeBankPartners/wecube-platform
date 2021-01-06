package com.webank.wecube.platform.core.dto.plugin;

public class PageableDto{
	private int startIndex;
	private int pageSize = 10000;
	
	public PageableDto() {
	}
	
	public PageableDto(int startIndex,int pageSize) {
		this.startIndex = startIndex;
		this.pageSize = pageSize;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
}

