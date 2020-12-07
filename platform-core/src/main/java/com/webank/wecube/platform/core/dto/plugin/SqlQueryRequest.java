package com.webank.wecube.platform.core.dto.plugin;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class SqlQueryRequest extends ResourceQueryRequest{
    private String sqlQuery;
    
    public SqlQueryRequest() {
    }
    
    public SqlQueryRequest(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }
    
    public String getSqlQuery() {
        return sqlQuery;
    }
    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }
    
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this).build();
    }
}
