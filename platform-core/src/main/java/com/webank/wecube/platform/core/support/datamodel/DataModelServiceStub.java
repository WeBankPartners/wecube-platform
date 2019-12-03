package com.webank.wecube.platform.core.support.datamodel;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.UrlToResponseDto;
import com.webank.wecube.platform.core.parser.datamodel.DataModelExpressionParser;
import com.webank.wecube.platform.core.utils.JsonUtils;
import com.webank.wecube.platform.core.utils.RestTemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataModelServiceStub {

    public static final String CHAIN_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}?filter={attributeName},{value}";
    public static final String CREATE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/create";
    public static final String RETRIEVE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}";
    public static final String RETRIEVE_REQUEST_WITH_FILTER_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}?{requestParams}";
    public static final String UPDATE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/update";
    public static final String DELETE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/delete";
    public static final String UNIQUE_IDENTIFIER = "id";

    private static final Logger logger = LoggerFactory.getLogger(DataModelServiceStub.class);

    private RestTemplate restTemplate;
    private HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    public DataModelServiceStub(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Check response from a http request
     *
     * @param response response from http request
     * @return transferred commonResponseDto from response
     * @throws WecubeCoreException while JsonUtils transferring response to CommonResponseDto class
     */
    private static CommonResponseDto checkResponse(ResponseEntity<String> response) throws WecubeCoreException {
        CommonResponseDto responseDto;
        if (StringUtils.isEmpty(response.getBody()) || response.getStatusCode().isError()) {
            if (response.getStatusCode().is4xxClientError()) {
                throw new WecubeCoreException(String.format("The target server returned error code: [%s]. The target server doesn't implement the request controller.", response.getStatusCode().toString()));
            }

            if (response.getStatusCode().is5xxServerError()) {
                throw new WecubeCoreException(String.format("The target server returned error code: [%s], which is an target server's internal error.", response.getStatusCode().toString()));
            }
        }
        try {
            responseDto = JsonUtils.toObject(response.getBody(), CommonResponseDto.class);
        } catch (IOException e) {
            String msg = "Cannot transfer response from target server to CommonResponseDto class, the target server doesn't standardize the response style.";
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }

        if (!CommonResponseDto.STATUS_OK.equals(responseDto.getStatus())) {
            String msg = String.format("Request error! The error message is [%s]", responseDto.getMessage());
            logger.error(msg);
            throw new WecubeCoreException(msg);
        }
        return responseDto;
    }

    /**
     * Issue a request from request url with place holders and param map
     *
     * @param requestUrl request url with place holders
     * @param paramMap   generated param map
     * @return common response dto
     */
    public UrlToResponseDto initiateGetRequest(String requestUrl, Map<String, Object> paramMap) {
        UrlToResponseDto responseDto;

        // combine url with param map
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(requestUrl);
        UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(paramMap);
        String uriStr = uriComponents.toString();
        responseDto = sendGetRequest(uriStr);

        return responseDto;
    }

    /**
     * Issue a request from request url with place holders and param map
     *
     * @param requestUrl request url with place holders
     * @param paramMap   generated param map
     * @Param chainRequestDto chain request dto scope
     */
    public UrlToResponseDto initiatePostRequest(String requestUrl, Map<String, Object> paramMap, List<Map<String, Object>> requestBodyParamMap) {
        UrlToResponseDto urlToResponseDto;
        // combine url with param map
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(requestUrl);
        UriComponents uriComponents = uriComponentsBuilder.buildAndExpand(paramMap);
        String uriStr = uriComponents.toString();
        urlToResponseDto = sendPostRequest(uriStr, requestBodyParamMap);
        return urlToResponseDto;
    }

    /**
     * Send request then transfer the response to common response dto
     *
     * @param uriStr bind and expanded uri string
     * @return common response dto
     */
    public UrlToResponseDto sendGetRequest(String uriStr) {

        logger.info(String.format("Sending GET request to target url: [%s]", uriStr));
        ResponseEntity<String> response;
        CommonResponseDto responseDto;
        response = RestTemplateUtils.sendGetRequestWithParamMap(this.restTemplate, uriStr, this.httpHeaders);
        responseDto = checkResponse(response);
        return new UrlToResponseDto(uriStr, responseDto);
    }

    /**
     * Send request then transfer the response to common response dto
     *
     * @param uriStr bind and expanded uri string
     * @return common response dto
     */
    public UrlToResponseDto sendPostRequest(String uriStr, List<Map<String, Object>> postRequestBodyParamMap) {
        logger.info(String.format("Sending POST request to target url: [%s] with request body: [%s]", uriStr, postRequestBodyParamMap));
        ResponseEntity<String> response;
        CommonResponseDto responseDto;
        response = RestTemplateUtils.sendPostRequestWithParamMap(this.restTemplate, uriStr, this.httpHeaders, postRequestBodyParamMap);
        responseDto = checkResponse(response);
        return new UrlToResponseDto(uriStr, responseDto);
    }

    /**
     * Generation of fetch data url param map
     *
     * @param gatewayUrl    gate way url
     * @param packageName   package name
     * @param entityName    entity name
     * @param attributeName attribute name
     * @param value         value
     * @return response map
     */
    public Map<String, Object> generateGetUrlParamMap(Object gatewayUrl,
                                                      Object packageName,
                                                      Object entityName,
                                                      Object attributeName,
                                                      Object value) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        paramMap.put("attributeName", attributeName);
        paramMap.put("value", value);
        return paramMap;
    }

    /**
     * Generation of fetch all entity data url param map
     *
     * @param gatewayUrl  gate way url
     * @param packageName package name
     * @param entityName  entity name
     * @return response map
     */
    public Map<String, Object> generateGetAllParamMap(Object gatewayUrl,
                                                      Object packageName,
                                                      Object entityName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        return paramMap;
    }

    public Map<String, Object> generateGetUrlParamMapWithFilters(String gatewayUrl, String packageName, String entityName, Map<String, String> allFilters) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        paramMap.put("requestParams", allFilters.entrySet()
                .stream()
                .map(stringStringEntry -> stringStringEntry.getKey() + "=" + stringStringEntry.getValue())
                .collect(Collectors.joining("&")));
        return paramMap;
    }

    /**
     * Generation of data write back url param map
     *
     * @param gatewayUrl  gateway url
     * @param packageName package name
     * @param entityName  entity name
     * @return generated param map for url binding
     */
    public Map<String, Object> generatePostUrlParamMap(Object gatewayUrl,
                                                       Object packageName,
                                                       Object entityName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        return paramMap;

    }

    /**
     * Generation of data write back body param map
     *
     * @param entityId       gateway url
     * @param attributeName  package name
     * @param attributeValue entity name
     * @return generated param map for url binding
     */
    public List<Map<String, Object>> generatePostBodyParamMap(Object entityId,
                                                              String attributeName,
                                                              Object attributeValue) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(UNIQUE_IDENTIFIER, entityId);
        paramMap.put(attributeName, attributeValue);
        return Collections.singletonList(paramMap);

    }

    /**
     * Get response's id data from given attribute key and value
     *
     * @param lastRequestResponseDto last request's response dto
     * @param requestAttributeName   key of filter
     * @param requestAttributeValue  value of filter
     * @return found responseId list
     * @throws WecubeCoreException throws exception when there is an error converting response to LinkedHashMap
     */
    @SuppressWarnings("unchecked")
    public List<Object> getResponseIdFromAttribute(CommonResponseDto lastRequestResponseDto, String requestAttributeName, Object requestAttributeValue) throws WecubeCoreException {
        List<Object> result = new ArrayList<>();
        List<Object> requestResponseDataList = this.extractValueFromResponse(lastRequestResponseDto, DataModelExpressionParser.FETCH_ALL);
        requestResponseDataList.forEach(o -> {

            if (!(o instanceof LinkedHashMap<?, ?>)) {
                String msg = "Cannot transfer lastRequestResponse list to LinkedHashMap.";
                logger.error(msg, lastRequestResponseDto, requestAttributeName, requestAttributeValue);
                throw new WecubeCoreException(msg);
            }
            LinkedHashMap<String, Object> requestResponseDataMap = (LinkedHashMap<String, Object>) o;
            if (requestAttributeValue.equals(requestResponseDataMap.get(requestAttributeName))) {
                result.add(requestResponseDataMap.get(DataModelServiceStub.UNIQUE_IDENTIFIER));
            }
        });

        return result;
    }

    /**
     * Handle response and resolve it to list of objects
     *
     * @param responseDto   common response dto
     * @param attributeName the attribute name the expression want to fetch
     * @return list of value fetched from expression
     */
    public List<Object> extractValueFromResponse(CommonResponseDto responseDto, String attributeName) {
        // transfer dto to List<LinkedTreeMap>
        List<Object> returnList = new ArrayList<>();
        List<Map<String, Object>> dataArray = responseToMapList(responseDto);

        logger.info(String.format("Extract value from given http request's response [%s] by attribute name: [%s]", dataArray, attributeName));

        switch (attributeName) {
            case DataModelExpressionParser.FETCH_ALL: {
                returnList = Objects.requireNonNull(dataArray)
                        .stream()
                        .sorted(Comparator.comparing(o -> String.valueOf(o.get(DataModelServiceStub.UNIQUE_IDENTIFIER))))
                        .collect(Collectors.toList());
                break;
            }
            case DataModelExpressionParser.FETCH_NONE: {
                break;
            }
            default: {
                returnList = Objects.requireNonNull(dataArray)
                        .stream()
                        .sorted(Comparator.comparing(o -> String.valueOf(o.get(DataModelServiceStub.UNIQUE_IDENTIFIER))))
                        .map(linkedTreeMap -> linkedTreeMap.get(attributeName))
                        .collect(Collectors.toList());
                break;
            }
        }

        logger.info(String.format("The extraction from request's response by given attribute name [%s] is [%s]", attributeName, returnList));

        return returnList;
    }

    /**
     * Handle response and resolve it to list of objects
     *
     * @param responseDto common response dto
     * @return list of value fetched from expression
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> responseToMapList(CommonResponseDto responseDto) {
        List<Map<String, Object>> dataArray = new ArrayList<>();
        Object data = responseDto.getData();

        if (data instanceof ArrayList<?>) {
            dataArray = (List<Map<String, Object>>) data;
        } else if (data instanceof LinkedHashMap<?, ?>) {
            dataArray.add((Map<String, Object>) data);
        }

        return dataArray;
    }
}
