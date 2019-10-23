package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationQuery {
    private boolean paging;
    private Pageable pageable;
    private String filterRs;
    private List<Filter> filters = new ArrayList<>();
    private Sorting sorting;
    private List<String> groupBys = new ArrayList<>();
    private List<String> refResources = new ArrayList<>();
    private List<String> resultColumns;
    private Dialect dialect = new Dialect();

    public static class Dialect {
        private boolean showCiHistory = false;
        private Map<String, Object> data;

        public boolean getShowCiHistory() {
            return showCiHistory;
        }

        public void setShowCiHistory(boolean showCiHistory) {
            this.showCiHistory = showCiHistory;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

    }

    public PaginationQuery addEqualsFilter(String name, Object value) {
        filters.add(new Filter(name, "eq", value));
        return this;
    }

    public PaginationQuery setFiltersRelationship(String relationship) {
        filterRs = relationship;
        return this;
    }

    public PaginationQuery addEqualsFilters(Map<String, Object> filterObject) {
        filterObject.entrySet().forEach(entry -> filters.add(new Filter(entry.getKey(), "eq", entry.getValue())));
        return this;
    }

    public PaginationQuery addInFilters(Map<String, Object> filterObject) {
        filterObject.entrySet().forEach(entry -> filters.add(new Filter(entry.getKey(), "in", entry.getValue())));
        return this;
    }

    public PaginationQuery addNotEqualsFilter(String name, Object value) {
        filters.add(new Filter(name, "ne", value));
        return this;
    }

    public PaginationQuery addInFilter(String name, List values) {
        filters.add(new Filter(name, "in", values));
        return this;
    }

    public PaginationQuery addContainsFilter(String name, String value) {
        filters.add(new Filter(name, "contains", value));
        return this;
    }

    public PaginationQuery addNotNullFilter(String name) {
        filters.add(new Filter(name, "notNull", null));
        return this;
    }

    public PaginationQuery addNullFilter(String name) {
        filters.add(new Filter(name, "null", null));
        return this;
    }

    public PaginationQuery ascendingSortBy(String field) {
        sorting = new Sorting(true, field);
        return this;
    }

    public PaginationQuery descendingSortBy(String field) {
        sorting = new Sorting(false, field);
        return this;
    }

    public PaginationQuery addReferenceResource(String refResource) {
        refResources.add(refResource);
        return this;
    }

    public PaginationQuery withResultColumns(List<String> resultColumns) {
        this.resultColumns = resultColumns;
        return this;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        private String name;
        private String operator;
        private Object value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sorting {
        private boolean asc;
        private String field;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pageable {
        private int startIndex;
        private int pageSize;
    }

    public static PaginationQuery defaultQueryObject() {
        return new PaginationQuery();
    }

    public static PaginationQuery defaultQueryObject(String name, Object value) {
        return defaultQueryObject().addEqualsFilter(name, value);
    }

    @Override
    public String toString() {
        return "PaginationQuery [paging=" + paging + ", pageable=" + pageable + ", filters=" + filters + ", sorting="
                + sorting + ", groupBys=" + groupBys + ", refResources=" + refResources + ", resultColumns="
                + resultColumns + "]";
    }


}
