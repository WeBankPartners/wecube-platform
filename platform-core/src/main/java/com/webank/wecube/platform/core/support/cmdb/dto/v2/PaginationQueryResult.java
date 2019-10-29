package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import lombok.Data;

import java.util.List;

@Data
public class PaginationQueryResult<DATATYPE> {
    private PageInfo pageInfo;
    private List<DATATYPE> contents;

    @Data
    public static class PageInfo {
        private int startIndex;
        private int pageSize;
        private int totalRows;
    }
}
