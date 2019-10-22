package com.webank.wecube.platform.core.dto;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class QueryRequest {
    protected boolean paging = false;
    protected Pageable pageable = new Pageable();
    protected List<Filter> filters = new LinkedList<>();
    protected Sorting sorting = new Sorting();
    protected String filterRs = FilterRelationship.AND.getCode();

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public Sorting getSorting() {
        return sorting;
    }

    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
    }

    public boolean isPaging() {
        return paging;
    }

    public void setPaging(boolean paging) {
        this.paging = paging;
    }

    public String getFilterRs() {
        return filterRs;
    }

    public void setFilterRs(String filterRs) {
        this.filterRs = filterRs;
    }


    public QueryRequest addEqualsFilter(String name, Object value) {
        filters.add(new Filter(name, "eq", value));
        return this;
    }

    public QueryRequest addGreaterThanFilter(String name, Object value) {
        filters.add(new Filter(name, "gt", value));
        return this;
    }

    public QueryRequest addLessThanFilter(String name, Object value) {
        filters.add(new Filter(name, "lt", value));
        return this;
    }

    public QueryRequest addEqualsFilters(Map<String, Object> filterObject) {
        filterObject.entrySet().forEach(entry -> filters.add(new Filter(entry.getKey(), "eq", entry.getValue())));
        return this;
    }

    public QueryRequest addInFilters(Map<String, Object> filterObject) {
        filterObject.entrySet().forEach(entry -> filters.add(new Filter(entry.getKey(), "in", entry.getValue())));
        return this;
    }

    public QueryRequest addNotEqualsFilter(String name, Object value) {
        filters.add(new Filter(name, "ne", value));
        return this;
    }

    public QueryRequest addInFilter(String name, List values) {
        filters.add(new Filter(name, "in", values));
        return this;
    }

    public QueryRequest addContainsFilter(String name, String value) {
        filters.add(new Filter(name, "contains", value));
        return this;
    }

    public QueryRequest addNotNullFilter(String name) {
        filters.add(new Filter(name, "notNull", null));
        return this;
    }

    public QueryRequest addNullFilter(String name) {
        filters.add(new Filter(name, "null", null));
        return this;
    }

    public QueryRequest ascendingSortBy(String field) {
        sorting = new Sorting(true, field);
        return this;
    }

    public QueryRequest descendingSortBy(String field) {
        sorting = new Sorting(false, field);
        return this;
    }

    public static QueryRequest defaultQueryObject() {
        return new QueryRequest();
    }

    public static QueryRequest defaultQueryObject(String name, Object value) {
        return defaultQueryObject().addEqualsFilter(name, value);
    }
}
