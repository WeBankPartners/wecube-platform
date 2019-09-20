package com.webank.wecube.core.service.resource;

import com.webank.wecube.core.domain.ResourceItem;

public interface ResourceItemOperationService {
    void startItem(ResourceItem item);

    void stopItem(ResourceItem item);
}
