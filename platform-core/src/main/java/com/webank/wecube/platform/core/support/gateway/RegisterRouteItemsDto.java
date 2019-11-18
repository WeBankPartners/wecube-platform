package com.webank.wecube.platform.core.support.gateway;

import java.util.List;

public class RegisterRouteItemsDto {

    private String name;
    private List<RouteItem> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RouteItem> getItems() {
        return items;
    }

    public void setItems(List<RouteItem> items) {
        this.items = items;
    }

    public RegisterRouteItemsDto() {
    }

    public RegisterRouteItemsDto(String name, List<RouteItem> items) {
        this.name = name;
        this.items = items;
    }

}
