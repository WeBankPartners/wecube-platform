package com.webank.wecube.platform.core.service.datamodel;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.dto.UrlToResponseDto;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.support.datamodel.DataModelServiceStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NonExpressionServiceImpl implements NonExpressionService {
    private ApplicationProperties applicationProperties;
    private DataModelServiceStub dataModelServiceStub;

    @Autowired
    public NonExpressionServiceImpl(ApplicationProperties applicationProperties, DataModelServiceStub dataModelServiceStub) {
        this.applicationProperties = applicationProperties;
        this.dataModelServiceStub = dataModelServiceStub;
    }

    @Override
    public List<Map<String, Object>> createEntity(String packageName, String entityName, List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = dataModelServiceStub.generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        UrlToResponseDto urlToResponseDto = dataModelServiceStub.initiatePostRequest(DataModelServiceStub.CREATE_REQUEST_URL, postRequestUrlParamMap, request);
        return dataModelServiceStub.responseToMapList(urlToResponseDto.getResponseDto());
    }

    @Override
    public List<Object> retrieveEntity(String packageName, String entityName) {
        Map<String, Object> getAllUrlParamMap = dataModelServiceStub.generateGetAllParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        UrlToResponseDto urlToResponseDto = dataModelServiceStub.initiateGetRequest(DataModelServiceStub.RETRIEVE_REQUEST_URL, getAllUrlParamMap);
        return dataModelServiceStub.extractValueFromResponse(urlToResponseDto.getResponseDto(), DataModelExpressionParser.FETCH_ALL);
    }

    @Override
    public List<Map<String, Object>> updateEntity(String packageName, String entityName, List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = dataModelServiceStub.generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        UrlToResponseDto urlToResponseDto = dataModelServiceStub.initiatePostRequest(DataModelServiceStub.UPDATE_REQUEST_URL, postRequestUrlParamMap, request);
        return dataModelServiceStub.responseToMapList(urlToResponseDto.getResponseDto());
    }

    @Override
    public void deleteEntity(String packageName, String entityName, List<Map<String, Object>> request) {
        Map<String, Object> postRequestUrlParamMap = dataModelServiceStub.generatePostUrlParamMap(this.applicationProperties.getGatewayUrl(), packageName, entityName);
        dataModelServiceStub.initiatePostRequest(DataModelServiceStub.DELETE_REQUEST_URL, postRequestUrlParamMap, request);
    }
}
