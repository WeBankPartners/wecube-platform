package com.webank.wecube.platform.gateway.dto;

import java.util.ArrayList;
import java.util.List;

public class RouteItemPushDto {
    private String context;
    private List<RouteItemInfoDto> items = new ArrayList<>();

    public List<RouteItemInfoDto> getItems() {
        return items;
    }

    public void setItems(List<RouteItemInfoDto> items) {
        this.items = items;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

}
