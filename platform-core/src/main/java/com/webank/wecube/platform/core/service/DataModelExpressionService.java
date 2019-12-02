package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.support.datamodel.TreeNode;

import java.util.List;
import java.util.Map;

public interface DataModelExpressionService {

    List<Object> fetchData(DataModelExpressionToRootData expressionToRootData);

    List<Object> retrieveEntity(String packageName, String entityName);

    void writeBackData(DataModelExpressionToRootData expressionToRootData, Object updateData);

    List<TreeNode> getPreviewTree(DataModelExpressionToRootData expressionToRootData);

    List<Map<String, Object>> createEntity(String packageName, String entityName, List<Map<String, Object>> request);

    List<Map<String, Object>> updateEntity(String packageName, String entityName, List<Map<String, Object>> request);

    void deleteEntity(String packageName, String entityName, List<Map<String, Object>> request);
}
