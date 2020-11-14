package com.webank.wecube.platform.core.dto;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class QueryRequestDto {
    protected boolean paging = false;
    protected PageableDto pageable = new PageableDto();
    protected List<FilterDto> filters = new LinkedList<>();
    protected SortingDto sorting = new SortingDto();
    protected String filterRs = FilterRelationship.AND.getCode();

    public PageableDto getPageable() {
        return pageable;
    }

    public void setPageable(PageableDto pageable) {
        this.pageable = pageable;
    }

    public List<FilterDto> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDto> filters) {
        this.filters = filters;
    }

    public SortingDto getSorting() {
        return sorting;
    }

    public void setSorting(SortingDto sorting) {
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


    public QueryRequestDto addEqualsFilter(String name, Object value) {
        filters.add(new FilterDto(name, "eq", value));
        return this;
    }

    public QueryRequestDto addGreaterThanFilter(String name, Object value) {
        filters.add(new FilterDto(name, "gt", value));
        return this;
    }

    public QueryRequestDto addLessThanFilter(String name, Object value) {
        filters.add(new FilterDto(name, "lt", value));
        return this;
    }

    public QueryRequestDto addEqualsFilters(Map<String, Object> filterObject) {
        filterObject.entrySet().forEach(entry -> filters.add(new FilterDto(entry.getKey(), "eq", entry.getValue())));
        return this;
    }

    public QueryRequestDto addInFilters(Map<String, Object> filterObject) {
        filterObject.entrySet().forEach(entry -> filters.add(new FilterDto(entry.getKey(), "in", entry.getValue())));
        return this;
    }

    public QueryRequestDto addNotEqualsFilter(String name, Object value) {
        filters.add(new FilterDto(name, "ne", value));
        return this;
    }

    public QueryRequestDto addInFilter(String name, List<Object> values) {
        filters.add(new FilterDto(name, "in", values));
        return this;
    }

    public QueryRequestDto addContainsFilter(String name, String value) {
        filters.add(new FilterDto(name, "contains", value));
        return this;
    }

    public QueryRequestDto addNotNullFilter(String name) {
        filters.add(new FilterDto(name, "notNull", null));
        return this;
    }

    public QueryRequestDto addNullFilter(String name) {
        filters.add(new FilterDto(name, "null", null));
        return this;
    }

    public QueryRequestDto ascendingSortBy(String field) {
        sorting = new SortingDto(true, field);
        return this;
    }

    public QueryRequestDto descendingSortBy(String field) {
        sorting = new SortingDto(false, field);
        return this;
    }

    public static QueryRequestDto defaultQueryObject() {
        return new QueryRequestDto();
    }

    public static QueryRequestDto defaultQueryObject(String name, Object value) {
        return defaultQueryObject().addEqualsFilter(name, value);
    }
}
