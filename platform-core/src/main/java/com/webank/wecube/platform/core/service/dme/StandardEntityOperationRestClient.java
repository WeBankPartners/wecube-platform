package com.webank.wecube.platform.core.service.dme;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.utils.Constants;

public class StandardEntityOperationRestClient {
    private static final Logger log = LoggerFactory.getLogger(StandardEntityOperationRestClient.class);

    private String queryUriTemplate = "/{package-name}/entities/{entity-name}/query";
    private String updateUriTemplate = "/{package-name}/entities/{entity-name}/update";
    private String createUriTemplate = "/{package-name}/entities/{entity-name}/create";
    private String deleteUriTemplate = "/{package-name}/entities/{entity-name}/delete";
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();

    public StandardEntityOperationRestClient() {
        this.restTemplate = new RestTemplate();
    }

    public StandardEntityOperationRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public StandardEntityOperationResponseDto create(EntityRouteDescription entityDef,
            List<EntityDataRecord> recordsToCreate) {
        URI requestUri = buildStandardOperationUri(entityDef, getCreateUriTemplate());

        List<Map<String, Object>> requestBody = convertToMapList(recordsToCreate);

        long timeMilliSeconds = System.currentTimeMillis();
        log.info("SEND CREATE post [{}] url={}, request={}", timeMilliSeconds, requestUri.toString(),
                toJson(requestBody));
        StandardEntityOperationResponseDto result = getRestTemplate().postForObject(requestUri, requestBody,
                StandardEntityOperationResponseDto.class);
        log.debug("RECEIVE CREATE post [{}] url={},result={}", timeMilliSeconds, requestUri.toString(), result);
        return result;
    }

    public StandardEntityOperationResponseDto delete(EntityRouteDescription entityDef,
            List<EntityDataRecord> recordsToDelete) {
        URI requestUri = buildStandardOperationUri(entityDef, getDeleteUriTemplate());

        List<Map<String, Object>> requestBody = convertToMapList(recordsToDelete);

        long timeMilliSeconds = System.currentTimeMillis();
        log.info("SEND DELETE post [{}] url={}, request={}", timeMilliSeconds, requestUri.toString(),
                toJson(requestBody));
        StandardEntityOperationResponseDto result = getRestTemplate().postForObject(requestUri, requestBody,
                StandardEntityOperationResponseDto.class);
        log.debug("RECEIVE DELETE post [{}] url={},result={}", timeMilliSeconds, requestUri.toString(), result);
        return result;
    }

    public StandardEntityOperationResponseDto query(EntityRouteDescription entityDef,
            EntityQuerySpecification querySpec) {
        URI requestUri = buildStandardOperationUri(entityDef, getQueryUriTemplate());

        long timeMilliSeconds = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info("SEND QUERY post [{}] url={}, request={}", timeMilliSeconds, requestUri.toString(),
                    toJson(querySpec));
        }
        StandardEntityOperationResponseDto result = getRestTemplate().postForObject(requestUri, querySpec,
                StandardEntityOperationResponseDto.class);
        if (log.isDebugEnabled()) {
            log.debug("RECEIVE QUERY post [{}] url={},result={}", timeMilliSeconds, requestUri.toString(), result);
        }
        return result;
    }

    // POST List<Map<String, Object>>
    public StandardEntityOperationResponseDto update(EntityRouteDescription entityDef,
            List<EntityDataRecord> recordsToUpdate) {
        URI requestUri = buildStandardOperationUri(entityDef, getUpdateUriTemplate());

        List<Map<String, Object>> requestBody = convertToMapList(recordsToUpdate);
        long timeMilliSeconds = System.currentTimeMillis();
        log.info("SEND UPDATE post [{}] url={}, request={}", timeMilliSeconds, requestUri.toString(),
                toJson(requestBody));
        StandardEntityOperationResponseDto result = getRestTemplate().postForObject(requestUri, requestBody,
                StandardEntityOperationResponseDto.class);
        log.debug("RECEIVE UPDATE post [{}] url={},result={}", timeMilliSeconds, requestUri.toString(), result);
        if(!StandardEntityOperationResponseDto.STATUS_OK.equalsIgnoreCase(result.getStatus())) {
            log.error("update failed with error:{} {}", result.getStatus(), result.getMessage());
            throw new WecubeCoreException(result.getMessage());
        }
        return result;
    }
    
    public StandardEntityOperationResponseDto updateData(EntityRouteDescription entityDef,List<Map<String, Object>> recordsToUpdate) {
        URI requestUri = buildStandardOperationUri(entityDef, getUpdateUriTemplate());

        long timeMilliSeconds = System.currentTimeMillis();
        log.info("SEND UPDATE post [{}] url={}, request={}", timeMilliSeconds, requestUri.toString(),
                toJson(recordsToUpdate));
        StandardEntityOperationResponseDto result = getRestTemplate().postForObject(requestUri, recordsToUpdate,
                StandardEntityOperationResponseDto.class);
        log.debug("RECEIVE UPDATE post [{}] url={},result={}", timeMilliSeconds, requestUri.toString(), result);
        
        if(!StandardEntityOperationResponseDto.STATUS_OK.equalsIgnoreCase(result.getStatus())) {
            log.error("update failed with error:{} {}", result.getStatus(), result.getMessage());
            throw new WecubeCoreException(result.getMessage());
        }
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

    protected URI buildStandardOperationUri(EntityRouteDescription entityDef, String operationUriTemplate) {
        String baseUri = buildBaseRequestUri(entityDef);
        String requestUriStr = buildRequestUri(baseUri, operationUriTemplate);
        URI requestUri = getRestTemplate().getUriTemplateHandler().expand(requestUriStr, entityDef.getPackageName(),
                entityDef.getEntityName());

        return requestUri;
    }

    private String buildRequestUri(String baseUri, String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return baseUri + path;
    }

    private String buildBaseRequestUri(EntityRouteDescription entityDef) {
        StringBuilder builder = new StringBuilder();
        builder.append(entityDef.getHttpScheme()).append("://");
        builder.append(entityDef.getHttpHost());
        if (entityDef.getHttpPort() != null && entityDef.getHttpPort().trim().length() > 0) {
            builder.append(":").append(entityDef.getHttpPort().trim());
        }
        return builder.toString();
    }

    private List<Map<String, Object>> convertToMapList(List<EntityDataRecord> records) {
        if (records == null) {
            return null;
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (EntityDataRecord record : records) {
            Map<String, Object> paramMap = new HashMap<>();
            if (StringUtils.isNoneBlank(record.getId())) {
                paramMap.put(Constants.UNIQUE_IDENTIFIER, record.getId());
            }
            if (record.getAttrs() != null) {
                for (EntityDataAttr attr : record.getAttrs()) {
                    paramMap.put(attr.getAttrName(), attr.getAttrValue());
                }
            }

            mapList.add(paramMap);

        }

        return mapList;
    }

    private String toJson(Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            return json;
        } catch (JsonProcessingException e) {
            log.info("errors to convert json object", e);
            return "";
        }
    }

    public String getCreateUriTemplate() {
        return createUriTemplate;
    }

    public String getDeleteUriTemplate() {
        return deleteUriTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}
