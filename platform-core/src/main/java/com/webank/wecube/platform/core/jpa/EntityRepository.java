package com.webank.wecube.platform.core.jpa;

import com.webank.wecube.platform.core.dto.QueryRequestDto;
import com.webank.wecube.platform.core.dto.QueryResponse;

public interface EntityRepository {
    <T> QueryResponse<T> query(Class<T> domainClzz, QueryRequestDto ciRequest);
}
