package com.webank.wecube.core.service.resource;

import com.webank.wecube.core.domain.ResourceItem;

public interface ResourceItemOperationService {
    int startItem(ResourceItem item);

    int stopItem(ResourceItem item);
}
