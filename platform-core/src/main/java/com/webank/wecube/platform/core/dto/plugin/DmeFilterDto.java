package com.webank.wecube.platform.core.dto.plugin;

import java.util.List;

public class DmeFilterDto {
    String dataModelExpression;
    List<DmeLinkFilterDto> filters;

    public DmeFilterDto() {
    }

    public DmeFilterDto(String dataModelExpression, List<DmeLinkFilterDto> filters) {
        this.dataModelExpression = dataModelExpression;
        this.filters = filters;
    }

    public String getDataModelExpression() {
        return dataModelExpression;
    }

    public void setDataModelExpression(String dataModelExpression) {
        this.dataModelExpression = dataModelExpression;
    }

    public List<DmeLinkFilterDto> getFilters() {
        return filters;
    }

    public void setFilters(List<DmeLinkFilterDto> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DmeFilterDto [dataModelExpression=");
        builder.append(dataModelExpression);
        builder.append(", filters=");
        builder.append(filters);
        builder.append("]");
        return builder.toString();
    }
    
    
}
