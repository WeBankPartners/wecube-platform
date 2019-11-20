package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;

import java.util.List;
import java.util.Map;

public interface DataModelExpressionService {

    List<Object> fetchData(DataModelExpressionToRootData expressionToRootData);

    void writeBackData(DataModelExpressionToRootData expressionToRootData, Map<String, Object> updateData);

}
