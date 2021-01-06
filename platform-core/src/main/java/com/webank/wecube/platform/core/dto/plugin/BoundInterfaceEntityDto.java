package com.webank.wecube.platform.core.dto.plugin;

public class BoundInterfaceEntityDto {
    private String packageName;
    private String entityName;
    private String filterRule;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public BoundInterfaceEntityDto() {
        super();
    }

    public BoundInterfaceEntityDto(String packageName, String entityName, String filterRule) {
        super();
        this.packageName = packageName;
        this.entityName = entityName;
        this.filterRule = filterRule;
    }

    public String getFilterRule() {
        return filterRule;
    }

    public void setFilterRule(String filterRule) {
        this.filterRule = filterRule;
    }

}
