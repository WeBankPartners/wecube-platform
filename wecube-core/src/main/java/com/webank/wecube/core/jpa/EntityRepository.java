package com.webank.wecube.core.jpa;

import com.webank.wecube.core.dto.QueryRequest;
import com.webank.wecube.core.dto.QueryResponse;

public interface EntityRepository {
    <T> QueryResponse<T> query(Class<T> domainClzz, QueryRequest ciRequest);
}
