package com.webank.wecube.platform.core.service.dme;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class StandardEntityOperationRestClient {
    private static final Logger log = LoggerFactory.getLogger(StandardEntityOperationRestClient.class);
    public static final String CHAIN_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}?filter={attributeName},{value}";
    public static final String CREATE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/create";
    public static final String RETRIEVE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}";
    public static final String RETRIEVE_REQUEST_WITH_FILTER_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}?{requestParams}";
    public static final String UPDATE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/update";
    public static final String DELETE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/delete";

    private String queryUriTemplate = "/{package-name}/entities/{entity-name}/query";
    private String updateUriTemplate = "/{package-name}/entities/{entity-name}/update";
    private RestTemplate restTemplate;

    public StandardEntityOperationRestClient() {
    }

    public StandardEntityOperationRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public StandardEntityOperationResponseDto query(EntityDescription entityDef, EntityQuerySpecification querySpec) {
        String baseUri = buildBaseRequestUri(entityDef);
        String requestUriStr = buildRequestUri(baseUri, getQueryUriTemplate());
        URI requestUri = getRestTemplate().getUriTemplateHandler().expand(requestUriStr, entityDef.getPackageName(),
                entityDef.getEntityName());

        long timeMilliSeconds = System.currentTimeMillis();
        log.info("SEND QUERY post [{}] url={}, request={}", timeMilliSeconds, requestUri.toString(), querySpec);
        StandardEntityOperationResponseDto result = getRestTemplate().postForObject(requestUri, querySpec,
                StandardEntityOperationResponseDto.class);
        log.info("RECEIVE QUERY post [{}] url={},result={}", timeMilliSeconds, requestUri.toString(), result);
        return result;
    }

    // POST List<Map<String, Object>>
    public StandardEntityOperationResponseDto update(EntityDescription entityDef,
            List<EntityDataRecord> recordsToUpdate) {
        String baseUri = buildBaseRequestUri(entityDef);
        String requestUriStr = buildRequestUri(baseUri, getUpdateUriTemplate());
        URI requestUri = getRestTemplate().getUriTemplateHandler().expand(requestUriStr, entityDef.getPackageName(),
                entityDef.getEntityName());
        
        List<Map<String, Object>> requestBody = convertToMapList(recordsToUpdate);
        long timeMilliSeconds = System.currentTimeMillis();
        log.info("SEND UPDATE post [{}] url={}, request={}", timeMilliSeconds, requestUri.toString(), requestBody);
        StandardEntityOperationResponseDto result = getRestTemplate().postForObject(requestUri, requestBody,
                StandardEntityOperationResponseDto.class);
        log.info("RECEIVE UPDATE post [{}] url={},result={}", timeMilliSeconds, requestUri.toString(), result);
        return result;
    }

    public StandardEntityOperationRestClient withQueryUriTemplate(String queryUriTemplate) {
        this.queryUriTemplate = queryUriTemplate;
        return this;
    }

    public StandardEntityOperationRestClient withUpdateUriTemplate(String updateUriTemplate) {
        this.updateUriTemplate = updateUriTemplate;
        return this;
    }

    public StandardEntityOperationRestClient withRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        return this;
    }

    public String getQueryUriTemplate() {
        return queryUriTemplate;
    }

    public String getUpdateUriTemplate() {
        return updateUriTemplate;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    private String buildRequestUri(String baseUri, String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return baseUri + path;
    }

    private String buildBaseRequestUri(EntityDescription entityDef) {
        StringBuilder builder = new StringBuilder();
        builder.append(entityDef.getHttpSchema()).append("://");
        builder.append(entityDef.getHttpHost());
        if (entityDef.getHttpPort() != null && entityDef.getHttpPort().trim().length() > 0) {
            builder.append(":").append(entityDef.getHttpPort().trim());
        }
        return builder.toString();
    }

    private List<Map<String, Object>> convertToMapList(List<EntityDataRecord> recordsToUpdate) {
        if (recordsToUpdate == null) {
            return null;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (EntityDataRecord record : recordsToUpdate) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(EntityDataDelegate.UNIQUE_IDENTIFIER, record.getId());
            if (record.getAttrs() != null) {
                for (EntityDataAttr attr : record.getAttrs()) {
                    paramMap.put(attr.getAttrName(), attr.getAttrValue());
                }
            }
            
            mapList.add(paramMap);

        }

        return mapList;
    }

}
