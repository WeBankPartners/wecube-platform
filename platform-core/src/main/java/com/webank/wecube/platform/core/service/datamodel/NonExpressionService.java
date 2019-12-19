package com.webank.wecube.platform.core.service.datamodel;

import java.util.List;
import java.util.Map;

public interface NonExpressionService {
    List<Map<String, Object>> createEntity(String packageName, String entityName, List<Map<String, Object>> request);

    List<Object> retrieveEntity(String packageName, String entityName, Map<String, String> filterString);

    List<Map<String, Object>> updateEntity(String packageName, String entityName, List<Map<String, Object>> request);

    void deleteEntity(String packageName, String entityName, List<Map<String, Object>> request);

}
