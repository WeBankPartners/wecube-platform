package com.webank.wecube.platform.core.dto;


public class PageInfo extends PageableDto {
    private int totalRows;

    public PageInfo() {
    }

    public PageInfo(int totalRows, int startIndex, int pageSize) {
        super(startIndex, pageSize);
        this.totalRows = totalRows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
}
