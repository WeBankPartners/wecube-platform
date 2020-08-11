package com.webank.wecube.platform.core.support.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.Filter;
import com.webank.wecube.platform.core.dto.UrlToResponseDto;
import com.webank.wecube.platform.core.utils.FilterUtils;
import com.webank.wecube.platform.core.utils.RestTemplateUtils;

@Service
public class DataModelServiceStub {
    public final static String FETCH_ALL = "ALL";
    public final static String FETCH_NONE = "NONE";

    public static final String CHAIN_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}?filter={attributeName},{value}";
    public static final String CREATE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/create";
    public static final String RETRIEVE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}";
    public static final String RETRIEVE_REQUEST_WITH_FILTER_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}?{requestParams}";
    public static final String UPDATE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/update";
    public static final String DELETE_REQUEST_URL = "http://{gatewayUrl}/{packageName}/entities/{entityName}/delete";
    public static final String UNIQUE_IDENTIFIER = "id";
    public static final String VISUAL_FIELD = "displayName";

    private static final Logger logger = LoggerFactory.getLogger(DataModelServiceStub.class);

    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;

    @Autowired
    @Qualifier(value = "userJwtSsoTokenRestTemplate")
    private RestTemplate userJwtSsoTokenRestTemplate;

    /**
     * Issue a request from request url with place holders and param map
     *
     * @param requestUrl
     *            request url with place holders
     * @param paramMap
     *            generated param map
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
     * @param requestUrl
     *            request url with place holders
     * @param paramMap
     *            generated param map
     * @Param chainRequestDto chain request dto scope
     */
    public UrlToResponseDto initiatePostRequest(String requestUrl, Map<String, Object> paramMap,
            List<Map<String, Object>> requestBodyParamMap) {
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
     * @param uriStr
     *            bind and expanded uri string
     * @return common response dto
     */
    private UrlToResponseDto sendGetRequest(String uriStr) {
        HttpHeaders httpHeaders = new HttpHeaders();

        logger.info(String.format("Sending GET request to target url: [%s]", uriStr));
        ResponseEntity<String> response;
        CommonResponseDto responseDto;
        response = RestTemplateUtils.sendGetRequestWithUrlParamMap(this.jwtSsoRestTemplate, uriStr, httpHeaders);
        responseDto = RestTemplateUtils.checkResponse(response);
        return new UrlToResponseDto(uriStr, responseDto);
    }

    /**
     * Send request then transfer the response to common response dto
     *
     * @param uriStr
     *            bind and expanded uri string
     * @return common response dto
     */
    private UrlToResponseDto sendPostRequest(String uriStr, List<Map<String, Object>> postRequestBodyParamMap) {
        HttpHeaders httpHeaders = new HttpHeaders();
        logger.info(String.format("Sending POST request to target url: [%s] with request body: [%s]", uriStr,
                postRequestBodyParamMap));
        ResponseEntity<String> response;
        CommonResponseDto responseDto;
        response = RestTemplateUtils.sendPostRequestWithBody(this.jwtSsoRestTemplate, uriStr, httpHeaders,
                postRequestBodyParamMap);
        responseDto = RestTemplateUtils.checkResponse(response);
        return new UrlToResponseDto(uriStr, responseDto);
    }

    /**
     * Generation of fetch data url param map
     *
     * @param gatewayUrl
     *            gate way url
     * @param packageName
     *            package name
     * @param entityName
     *            entity name
     * @param attributeName
     *            attribute name
     * @param value
     *            value
     * @return response map
     */
    public Map<String, Object> generateGetUrlParamMap(Object gatewayUrl, Object packageName, Object entityName,
            Object attributeName, Object value) {
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
     * @param gatewayUrl
     *            gate way url
     * @param packageName
     *            package name
     * @param entityName
     *            entity name
     * @return response map
     */
    public Map<String, Object> generateGetAllParamMap(Object gatewayUrl, Object packageName, Object entityName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        return paramMap;
    }

    public Map<String, Object> generateGetUrlParamMapWithFilters(String gatewayUrl, String packageName,
            String entityName, Map<String, String> allFilters) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        paramMap.put("requestParams",
                allFilters.entrySet().stream()
                        .map(stringStringEntry -> stringStringEntry.getKey() + "=" + stringStringEntry.getValue())
                        .collect(Collectors.joining("&")));
        return paramMap;
    }

    /**
     * Generation of data write back url param map
     *
     * @param gatewayUrl
     *            gateway url
     * @param packageName
     *            package name
     * @param entityName
     *            entity name
     * @return generated param map for url binding
     */
    public Map<String, Object> generatePostUrlParamMap(Object gatewayUrl, Object packageName, Object entityName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("gatewayUrl", gatewayUrl);
        paramMap.put("packageName", packageName);
        paramMap.put("entityName", entityName);
        return paramMap;

    }

    /**
     * Generation of data write back body param map
     *
     * @param entityId
     *            gateway url
     * @param attributeName
     *            package name
     * @param attributeValue
     *            entity name
     * @return generated param map for url binding
     */
    public List<Map<String, Object>> generatePostBodyParamMap(Object entityId, String attributeName,
            Object attributeValue) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(UNIQUE_IDENTIFIER, entityId);
        paramMap.put(attributeName, attributeValue);
        return Collections.singletonList(paramMap);

    }

    /**
     * Get response's id data from given attribute key and value
     *
     * @param lastRequestResponseDto
     *            last request's response dto
     * @param requestAttributeName
     *            key of filter
     * @param requestAttributeValue
     *            value of filter
     * @return found responseId list
     * @throws WecubeCoreException
     *             throws exception when there is an error converting response
     *             to LinkedHashMap
     */
    @SuppressWarnings("unchecked")
    public List<Object> getResponseIdFromAttribute(CommonResponseDto lastRequestResponseDto,
            String requestAttributeName, Object requestAttributeValue) throws WecubeCoreException {
        List<Object> result = new ArrayList<>();
        List<Object> requestResponseDataList = this.extractValueFromResponse(lastRequestResponseDto, FETCH_ALL);
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
     * @param responseDto
     *            common response dto
     * @param keyName
     *            the key name the expression want to fetch
     * @return list of value fetched from expression
     */
    public List<Object> extractValueFromResponse(CommonResponseDto responseDto, String keyName) {
        // transfer dto to List<LinkedTreeMap>
        List<Map<String, Object>> dataArray = responseToMapList(responseDto);

        logger.info(String.format("Extract value from given http request's response [%s] by attribute name: [%s]",
                dataArray, keyName));

        List<Object> returnList;
        switch (keyName) {
        case FETCH_ALL: {
            returnList = Objects.requireNonNull(dataArray).stream()
                    .sorted(Comparator.comparing(o -> String.valueOf(o.get(DataModelServiceStub.UNIQUE_IDENTIFIER))))
                    .collect(Collectors.toList());
            break;
        }
        case FETCH_NONE: {
            returnList = new ArrayList<>();
            break;
        }
        default: {
            returnList = Objects.requireNonNull(dataArray).stream()
                    .sorted(Comparator.comparing(o -> String.valueOf(o.get(DataModelServiceStub.UNIQUE_IDENTIFIER))))
                    .map(linkedTreeMap -> linkedTreeMap.get(keyName)).collect(Collectors.toList());
            break;
        }
        }

        logger.info(String.format("The extraction from request's response by given attribute name [%s] is [%s]",
                keyName, returnList));

        return returnList;
    }

    /**
     * Handle response and resolve it to list of objects
     *
     * @param responseDto
     *            common response dto
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

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> filterData(Object jsonData, List<Filter> filterList) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (jsonData instanceof Map) {
            result.addAll(filterData((Map<String, Object>) jsonData, filterList));
        }

        if (jsonData instanceof List) {
            result.addAll(filterData((List<Map<String, Object>>) jsonData, filterList));
        }
        return result;
    }

    private List<Map<String, Object>> filterData(List<Map<String, Object>> jsonData, List<Filter> filterList)
            throws WecubeCoreException {
        List<Predicate<Map<String, Object>>> predicateFilters;
        try {
            predicateFilters = FilterUtils.getPredicateList(filterList);
        } catch (IllegalAccessException e) {
            String msg = String.format("Cannot get filter predicate class from given filter list: [%s]",
                    filterList.toString());
            logger.error(msg);
            throw new WecubeCoreException("3303", msg, filterList.toString());
        }
        return jsonData.stream().filter(predicateFilters.stream().reduce(p -> true, Predicate::and))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> filterData(Map<String, Object> jsonData, List<Filter> filterList) {
        return this.filterData(Collections.singletonList(jsonData), filterList);
    }
}
