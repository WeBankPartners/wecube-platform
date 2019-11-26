package com.webank.wecube.platform.core.utils;

import com.webank.wecube.platform.core.commons.HttpRequestErrorHandler;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class RestTemplateUtils {


    /**
     * Send get request to url with params
     *
     * @param restTemplate restTemplate
     * @param requestUri   target uri
     * @param headers      request headers
     * @return String
     */
    public static ResponseEntity<String> sendGetRequestWithParamMap(RestTemplate restTemplate, String requestUri, HttpHeaders headers) {

        HttpMethod method = HttpMethod.GET;
        // set content type as form
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // setup http request entity
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        // send request and exchange the response to target class
        restTemplate.setErrorHandler(new HttpRequestErrorHandler());
        return restTemplate.exchange(requestUri, method, requestEntity, String.class);
    }

    /**
     * Send get request to url with params
     *
     * @param restTemplate restTemplate
     * @param requestUri   target uri
     * @param headers      request headers
     * @return String
     */
    public static ResponseEntity<String> sendGetRequestWithParamMap(RestTemplate restTemplate, URI requestUri, HttpHeaders headers) {
        return sendGetRequestWithParamMap(restTemplate, requestUri.getPath(), headers);
    }

    /**
     * Send get request to url without params
     *
     * @param restTemplate restTemplate
     * @param url          target url
     * @return String
     */
    public ResponseEntity<String> sendGetRequestWithoutParam(RestTemplate restTemplate, String url) {
        return restTemplate.getForEntity(url, String.class);
    }

    /**
     * Send post request to url with params
     *
     * @param restTemplate restTemplate
     * @param requestUri   target uri
     * @param headers      request headers
     * @return String
     */
    public static ResponseEntity<String> sendPostRequestWithParamMap(RestTemplate restTemplate, String requestUri, List<Map<String, Object>> requestParamMap, HttpHeaders headers) {

        HttpMethod method = HttpMethod.POST;
        // set content type as form
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        // setup http request entity
        HttpEntity<List<Map<String, Object>>> requestEntity = new HttpEntity<>(requestParamMap, headers);

        // send request and exchange the response to target class
        return restTemplate.exchange(requestUri, method, requestEntity, String.class);
    }
}
