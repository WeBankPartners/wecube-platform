package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.dto.DataModelExpressionDto;

import java.util.List;

public interface DataModelExpressionService {

    List<DataModelExpressionDto> fetchData(String gateWayUrl, String dataModelExpression, String rootIdName, String rootIdData);

}
