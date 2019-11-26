package com.webank.wecube.platform.core.service;

import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.support.datamodel.TreeNode;

import java.util.List;

public interface DataModelExpressionService {

    List<Object> fetchData(DataModelExpressionToRootData expressionToRootData);

    List<Object> targetEntityQuery(String packageName, String entityName);

    void writeBackData(DataModelExpressionToRootData expressionToRootData, Object updateData);

    List<TreeNode> getPreviewTree(DataModelExpressionToRootData expressionToRootData);

}
