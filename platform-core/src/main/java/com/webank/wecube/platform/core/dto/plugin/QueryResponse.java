package com.webank.wecube.platform.core.dto.plugin;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class QueryResponse<T> {
    private PageInfo pageInfo = null;
    private List<T> contents = new LinkedList<>();
    private List<String> headers = null;

    public QueryResponse() {
    }

    public QueryResponse(PageInfo pageInfo, List<T> contents) {
        this(pageInfo,contents,null);
    }

    public QueryResponse(PageInfo pageInfo, List<T> contents, List<String> headers) {
        this.pageInfo = pageInfo;
        this.contents = contents;
        this.headers = headers;
    }
    
    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public List<T> getContents() {
        return contents;
    }

    public void setContents(List<T> cis) {
        this.contents = cis;
    }

    public void addContent(T ciObj) {
        this.contents.add(ciObj);
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }


}
