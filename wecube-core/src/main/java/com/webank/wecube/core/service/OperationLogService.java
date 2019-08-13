package com.webank.wecube.core.service;

import com.google.common.collect.Lists;
import com.webank.wecube.core.domain.OperationLog;
import com.webank.wecube.core.dto.OperationLogDto;
import com.webank.wecube.core.dto.QueryRequest;
import com.webank.wecube.core.dto.QueryResponse;
import com.webank.wecube.core.jpa.EntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogService {

    @Autowired
    private EntityRepository entityRepository;

    public QueryResponse<OperationLogDto> query(QueryRequest queryRequest) {
        QueryResponse<OperationLog> queryResponse = entityRepository.query(OperationLog.class, queryRequest);

        List<OperationLogDto> OperationLogDtos = Lists.transform(queryResponse.getContents(), (x) -> OperationLogDto.fromDomain(x));
        return new QueryResponse<>(queryResponse.getPageInfo(), OperationLogDtos);
    }

}
