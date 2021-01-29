package com.webank.wecube.platform.core.service.datamodel;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.dto.plugin.UrlToResponseDto;
import com.webank.wecube.platform.core.support.datamodel.DataModelServiceStub;

@Service
public class NonExpressionServiceImpl implements NonExpressionService {
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private DataModelServiceStub dataModelServiceStub;

    @Override
    public List<Map<String, Object>> createEntity(String packageName, String entityName,
            List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = dataModelServiceStub
                .generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        UrlToResponseDto urlToResponseDto = dataModelServiceStub
                .initiatePostRequest(DataModelServiceStub.CREATE_REQUEST_URL, postRequestUrlParamMap, request);
        return dataModelServiceStub.responseToMapList(urlToResponseDto.getResponseDto());
    }

    @Override
    public List<Object> retrieveEntity(String packageName, String entityName, Map<String, String> allFilters) {
        UrlToResponseDto urlToResponseDto;
        if (allFilters.isEmpty()) {
            Map<String, Object> getAllUrlParamMap = dataModelServiceStub
                    .generateGetAllParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
            urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.RETRIEVE_REQUEST_URL,
                    getAllUrlParamMap);
        } else {
            Map<String, Object> getAllUrlParamMap = dataModelServiceStub.generateGetUrlParamMapWithFilters(
                    this.applicationProperties.getGatewayUrl(), packageName, entityName, allFilters);
            urlToResponseDto = dataModelServiceStub
                    .initiateGetRequest(DataModelServiceStub.RETRIEVE_REQUEST_WITH_FILTER_URL, getAllUrlParamMap);
        }

        return dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(),
                DataModelServiceStub.FETCH_ALL);
    }

    @Override
    public List<Map<String, Object>> updateEntity(String packageName, String entityName,
            List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = dataModelServiceStub
                .generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        UrlToResponseDto urlToResponseDto = dataModelServiceStub
                .initiatePostRequest(DataModelServiceStub.UPDATE_REQUEST_URL, postRequestUrlParamMap, request);
        return dataModelServiceStub.responseToMapList(urlToResponseDto.getResponseDto());
    }

    @Override
    public void deleteEntity(String packageName, String entityName, List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = dataModelServiceStub
                .generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        dataModelServiceStub.initiatePostRequest(DataModelServiceStub.DELETE_REQUEST_URL, postRequestUrlParamMap,
                request);
    }
}
