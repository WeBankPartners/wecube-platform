package com.webank.wecube.platform.core.dto.plugin;

import java.util.ArrayList;
import java.util.List;

public class DynamicDataModelPullResponseDto {
    private String status;
    private String message;
    private List<DynamicPluginEntityDto> data = new ArrayList<>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DynamicPluginEntityDto> getData() {
        return data;
    }

    public void setData(List<DynamicPluginEntityDto> data) {
        this.data = data;
    }

}
