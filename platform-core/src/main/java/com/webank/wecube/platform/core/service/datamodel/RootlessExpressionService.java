package com.webank.wecube.platform.core.service.datamodel;

import com.webank.wecube.platform.core.dto.DmeFilterDto;

import java.util.List;

public interface RootlessExpressionService {

    List<Object> fetchDataWithFilter(DmeFilterDto dmeFilterDto);
}
