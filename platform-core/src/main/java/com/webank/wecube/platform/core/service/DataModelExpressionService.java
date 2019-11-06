package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.dto.DataModelExpressionDto;

import java.util.List;
import java.util.Stack;

public interface DataModelExpressionService {

    List<Stack<DataModelExpressionDto>> fetchData(String gateWayUrl, List<String> dataModelExpression, List<String> rootIdData);

}
