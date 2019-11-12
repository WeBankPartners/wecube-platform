package com.webank.wecube.platform.core.utils;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

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
}
