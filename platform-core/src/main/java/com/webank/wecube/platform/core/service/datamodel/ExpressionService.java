package com.webank.wecube.platform.core.service.datamodel;

import com.webank.wecube.platform.core.dto.EntityDto;
import com.webank.wecube.platform.core.model.datamodel.DataModelExpressionToRootData;
import com.webank.wecube.platform.core.support.datamodel.dto.TreeNode;

import java.util.List;

public interface ExpressionService {

    List<Object> fetchData(DataModelExpressionToRootData expressionToRootData);

    void writeBackData(DataModelExpressionToRootData expressionToRootData, Object updateData);

    List<TreeNode> getPreviewTree(DataModelExpressionToRootData expressionToRootData);

    List<EntityDto> getAllEntities(String dataModelExpression);
}
