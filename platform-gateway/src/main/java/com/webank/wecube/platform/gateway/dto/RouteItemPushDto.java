package com.webank.wecube.platform.gateway.dto;

import java.util.ArrayList;
import java.util.List;

public class RouteItemPushDto {
    private String name;
    private List<RouteItemInfoDto> items = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RouteItemInfoDto> getItems() {
        return items;
    }

    public void setItems(List<RouteItemInfoDto> items) {
        this.items = items;
    }

}
