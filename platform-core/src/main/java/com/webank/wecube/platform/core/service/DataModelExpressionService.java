package com.webank.wecube.platform.core.service;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface DataModelExpressionService {

    List<List<String>> fetchData(String gateWayUrl, List<Pair<String, String>> expressionToRootIdDataPairList);

}
