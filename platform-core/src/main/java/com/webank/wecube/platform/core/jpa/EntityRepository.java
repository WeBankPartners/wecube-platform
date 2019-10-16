package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.dto.QueryRequest;
import com.webank.wecube.platform.core.dto.QueryResponse;

public interface EntityRepository {
    <T> QueryResponse<T> query(Class<T> domainClzz, QueryRequest ciRequest);
}
