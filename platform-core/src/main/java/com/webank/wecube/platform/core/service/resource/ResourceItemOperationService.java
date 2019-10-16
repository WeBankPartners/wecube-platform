package com.webank.wecube.platform.core.service.resource;

import com.webank.wecube.platform.core.domain.ResourceItem;

public interface ResourceItemOperationService {
    void startItem(ResourceItem item);

    void stopItem(ResourceItem item);
}
