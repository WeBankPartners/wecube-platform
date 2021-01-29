package com.webank.wecube.platform.core.service.datamodel;

import java.util.List;

import com.webank.wecube.platform.core.dto.plugin.DmeFilterDto;

public interface RootlessExpressionService {

    List<Object> fetchDataWithFilter(DmeFilterDto dmeFilterDto);
}
