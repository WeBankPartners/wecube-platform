package com.webank.wecube.platform.core.service.resource;

import com.webank.wecube.platform.core.entity.plugin.ResourceItem;

public interface ResourceItemService {
    ResourceItem createItem(ResourceItem item);

    ResourceItem retrieveItem(ResourceItem item);

    ResourceItem updateItem(ResourceItem item);

    void deleteItem(ResourceItem item);
}
