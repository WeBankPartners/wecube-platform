package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.dto.DataModelExpressionDto;

import java.util.Stack;

public interface DataModelExpressionService {

    Stack<DataModelExpressionDto> fetchData(String gateWayUrl, String dataModelExpression, String rootIdName, String rootIdData);

}
