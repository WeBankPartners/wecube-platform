package com.webank.wecube.platform.core.dto.plugin;

public class ResourceDataDto {
    private Object businessKeyValue;
    private String id;

    public Object getBusinessKeyValue() {
        return businessKeyValue;
    }

    public void setBusinessKeyValue(Object businessKeyValue) {
        this.businessKeyValue = businessKeyValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ResourceDataDto() {
    }

    public ResourceDataDto(Object businessKeyValue, String id) {
        super();
        this.businessKeyValue = businessKeyValue;
        this.id = id;
    }
}
