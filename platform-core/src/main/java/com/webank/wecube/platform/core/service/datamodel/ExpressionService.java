package com.webank.wecube.platform.core.service.datamodel;

import java.util.List;

import com.webank.wecube.platform.core.dto.plugin.EntityDto;

public interface ExpressionService {
    List<EntityDto> getAllEntities(String dataModelExpression);
}
