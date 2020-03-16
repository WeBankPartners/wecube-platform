package com.webank.wecube.platform.core.dto;

public class BindedInterfaceEntityDto {
    private String packageName;
    private String EntityName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEntityName() {
        return EntityName;
    }

    public void setEntityName(String entityName) {
        EntityName = entityName;
    }

    public BindedInterfaceEntityDto() {
        super();
    }

    public BindedInterfaceEntityDto(String packageName, String entityName) {
        super();
        this.packageName = packageName;
        EntityName = entityName;
    }

}
