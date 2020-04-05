package com.webank.wecube.platform.gateway.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author gavin
 *
 */
public class DynamicRouteItemInfoHolder {

    private static Map<String, List<DynamicRouteItemInfo>> routeItemsByName = new ConcurrentHashMap<String, List<DynamicRouteItemInfo>>();

    private static List<DynamicRouteItemInfo> badRouteItems = new LinkedList<>();

    private static volatile Long lastRefreshTime = 0L;

    public static List<DynamicRouteItemInfo> findByName(String name) {
        List<DynamicRouteItemInfo> dynamicRouteItems = routeItemsByName.get(name);
        if (dynamicRouteItems == null || dynamicRouteItems.isEmpty()) {
            return null;
        }
        
        List<DynamicRouteItemInfo> retList = new ArrayList<>();
        dynamicRouteItems.forEach(item -> {
            retList.add(item.clone());
        });

        return Collections.unmodifiableList(retList);
    }
    
    public static List<DynamicRouteItemInfo> findAll(){
        List<DynamicRouteItemInfo> items = new LinkedList<>();
        for(String name : routeItemsByName.keySet()){
            items.addAll(routeItemsByName.get(name));
        }
        
        return Collections.unmodifiableList(items);
    }
    
    public synchronized static void addDynamicRouteItemInfos(List<DynamicRouteItemInfo> rawRouteItemInfos){
        if (rawRouteItemInfos == null) {
            return;
        }

        if (rawRouteItemInfos.isEmpty()) {
            return;
        }
        
        for (DynamicRouteItemInfo rawItem : rawRouteItemInfos) {
            if (rawItem == null) {
                continue;
            }

            tryAddDynamicRouteItemInfo(rawItem);
        }
    }

    public synchronized static void refreshDynamicRouteItemInfos(List<DynamicRouteItemInfo> rawRouteItemInfos) {
        if (rawRouteItemInfos == null) {
            return;
        }

        if (rawRouteItemInfos.isEmpty()) {
            return;
        }

        tryClearBadRouteItems();

        for (DynamicRouteItemInfo rawItem : rawRouteItemInfos) {
            if (rawItem == null) {
                continue;
            }

            tryAddDynamicRouteItemInfo(rawItem);
        }

        tryClearOutdatedDynamicRouteItemInfos();

        lastRefreshTime = System.currentTimeMillis();
    }

    private static void tryClearBadRouteItems() {
        badRouteItems.clear();
    }

    private static void tryClearOutdatedDynamicRouteItemInfos() {
        for (String name : routeItemsByName.keySet()) {
            List<DynamicRouteItemInfo> outdatedItems = new ArrayList<>();
            List<DynamicRouteItemInfo> items = routeItemsByName.get(name);
            for (DynamicRouteItemInfo item : items) {
                if (item.getLastModifiedTime() <= lastRefreshTime) {
                    outdatedItems.add(item);
                }
            }

            items.removeAll(outdatedItems);

            routeItemsByName.put(name, items);
        }
    }

    private static void tryAddDynamicRouteItemInfo(DynamicRouteItemInfo rawItem) {
        List<DynamicRouteItemInfo> items = routeItemsByName.get(rawItem.getName());
        if (items == null) {
            items = new ArrayList<>();
            DynamicRouteItemInfo item = new DynamicRouteItemInfo();
            item.setAvailable(true);
            item.setCreateTime(System.currentTimeMillis());
            item.setHost(rawItem.getHost());
            item.setHttpSchema(rawItem.getHttpSchema());
            item.setItemId(UUID.randomUUID().toString());
            item.setLastModifiedTime(System.currentTimeMillis());
            item.setName(rawItem.getName());
            item.setOrderNo(0);
            item.setPort(rawItem.getPort());

            items.add(item);

            routeItemsByName.put(item.getName(), items);

            return;
        }

        DynamicRouteItemInfo existItem = findDynamicRouteItemInfoFromListByTemplate(items, rawItem);
        if (existItem == null) {
            DynamicRouteItemInfo item = new DynamicRouteItemInfo();
            item.setAvailable(true);
            item.setCreateTime(System.currentTimeMillis());
            item.setHost(rawItem.getHost());
            item.setHttpSchema(rawItem.getHttpSchema());
            item.setItemId(UUID.randomUUID().toString());
            item.setLastModifiedTime(System.currentTimeMillis());
            item.setName(rawItem.getName());
            item.setOrderNo(items.size());
            item.setPort(rawItem.getPort());

            items.add(item);

            return;
        }

        existItem.setLastModifiedTime(System.currentTimeMillis());

        return;
    }

    private static DynamicRouteItemInfo findDynamicRouteItemInfoFromListByTemplate(List<DynamicRouteItemInfo> items,
            DynamicRouteItemInfo rawItem) {
        for (DynamicRouteItemInfo item : items) {
            if (item.equals(rawItem)) {
                return item;
            }
        }

        return null;

    }

    public static List<DynamicRouteItemInfo> getBadRouteItemInfos() {
        return Collections.unmodifiableList(badRouteItems);
    }

    public static void disableDynamicRouteItemInfo(DynamicRouteItemInfo itemToDisable) {
        if (itemToDisable == null) {
            return;
        }

        if (itemToDisable.getItemId() == null) {
            return;
        }

        if (itemToDisable.getName() == null) {
            return;
        }

        List<DynamicRouteItemInfo> items = routeItemsByName.get(itemToDisable.getName());
        if (items == null || items.isEmpty()) {
            return;
        }

        DynamicRouteItemInfo item = findDynamicRouteItemInfoFromListById(items, itemToDisable.getItemId());
        if (item == null) {
            return;
        }

        item.setAvailable(false);
        item.setLastModifiedTime(System.currentTimeMillis());

        if (!badRouteItems.contains(item)) {
            badRouteItems.add(item);
        }

        items.remove(item);

        routeItemsByName.put(item.getName(), items);
    }

    private static DynamicRouteItemInfo findDynamicRouteItemInfoFromListById(List<DynamicRouteItemInfo> items,
            String itemId) {
        for (DynamicRouteItemInfo item : items) {
            if (itemId.equals(item.getItemId())) {
                return item;
            }
        }

        return null;
    }

}
