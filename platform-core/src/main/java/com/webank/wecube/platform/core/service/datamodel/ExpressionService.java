package com.webank.wecube.platform.core.service.datamodel;

import java.util.List;

import com.webank.wecube.platform.core.dto.EntityDto;

public interface ExpressionService {

//    List<Object> fetchData(DataModelExpressionToRootData expressionToRootData);

//    void writeBackData(DataModelExpressionToRootData expressionToRootData, Object updateData);

//    List<TreeNode> getPreviewTree(DataModelExpressionToRootData expressionToRootData);

    List<EntityDto> getAllEntities(String dataModelExpression);
}
