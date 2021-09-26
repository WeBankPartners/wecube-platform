package com.webank.wecube.platform.core.dto.workflow;

import java.util.List;

import com.webank.wecube.platform.core.dto.plugin.SortingDto;

public class WorkflowExecutionOverviewsQueryDto {
    private String startDate;
    private String endDate;
    private List<String> procDefNames;
    protected SortingDto sorting;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<String> getProcDefNames() {
        return procDefNames;
    }

    public void setProcDefNames(List<String> procDefNames) {
        this.procDefNames = procDefNames;
    }

    public SortingDto getSorting() {
        return sorting;
    }

    public void setSorting(SortingDto sorting) {
        this.sorting = sorting;
    }
}
