package com.webank.wecube.platform.gateway.route;

import java.util.ArrayList;
import java.util.List;

/**
 * NOT thread-safe
 * @author gavin
 *
 */
public class DynamicRouteContext {
    public static final String DYNAMIC_ROUTE_CONTEXT_KEY = "dynamic_route_context";

    private List<DynamicRouteItemInfo> routeItemInfos = new ArrayList<>();
    private volatile int currentIndex = -1;
    private volatile boolean hasNext = true;

    public DynamicRouteItemInfo next() {
        if (routeItemInfos.isEmpty()) {
            hasNext = false;
            return null;
        }

        currentIndex++;
        
        if (currentIndex >= routeItemInfos.size()) {
            hasNext = false;
            currentIndex = 0;
        }
        
        return routeItemInfos.get(currentIndex);

    }

    public DynamicRouteContext addDynamicRouteItemInfos(List<DynamicRouteItemInfo> items) {
        routeItemInfos.addAll(items);
        return this;
    }

    public boolean hasNext() {
        return hasNext;
    }
}
