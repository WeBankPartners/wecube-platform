package com.webank.wecube.platform.core.support.gateway;

import java.util.List;

public class RegisterRouteItemsDto {

    private String context;
    private List<RouteItem> items;

    
    public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public List<RouteItem> getItems() {
        return items;
    }

    public void setItems(List<RouteItem> items) {
        this.items = items;
    }

    public RegisterRouteItemsDto() {
    }

    public RegisterRouteItemsDto(String context, List<RouteItem> items) {
        this.context = context;
        this.items = items;
    }

}
