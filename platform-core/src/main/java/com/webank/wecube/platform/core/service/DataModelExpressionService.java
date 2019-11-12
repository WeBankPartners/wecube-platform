package com.webank.wecube.platform.core.service;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface DataModelExpressionService {

    List<Object> fetchData(String gateWayUrl, Pair<String, String> expressionToRootIdDataPairList);

}
