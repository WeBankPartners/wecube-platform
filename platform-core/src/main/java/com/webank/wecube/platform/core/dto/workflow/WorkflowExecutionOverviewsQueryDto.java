package com.webank.wecube.platform.core.dto.workflow;

import java.util.List;

import com.webank.wecube.platform.core.dto.plugin.SortingDto;

public class WorkflowExecutionOverviewsQueryDto {
    private String startDate;
    private String endDate;
    private List<String> procDefIds;
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
    
    public List<String> getProcDefIds() {
        return procDefIds;
    }

    public void setProcDefIds(List<String> procDefIds) {
        this.procDefIds = procDefIds;
    }

    public SortingDto getSorting() {
        return sorting;
    }

    public void setSorting(SortingDto sorting) {
        this.sorting = sorting;
    }
}
