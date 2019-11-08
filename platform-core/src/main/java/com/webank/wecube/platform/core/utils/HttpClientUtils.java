package com.webank.wecube.platform.core.utils;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class HttpClientUtils {

    /**
     * Send get request to url with params
     *
     * @param requestUri target uri
     * @param headers    request headers
     * @return String
     */
    public static ResponseEntity<String> sendGetRequestWithParamMap(String requestUri, HttpHeaders headers) {
        RestTemplate client = new RestTemplate();


        HttpMethod method = HttpMethod.GET;
        // set content type as form
        headers.setContentType(MediaType.APPLICATION_JSON);
        // setup http request entity
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        // send request and exchange the response to target class
        return client.exchange(requestUri, method, requestEntity, String.class);
    }

    /**
     * Send get request to url without params
     *
     * @param url target url
     * @return String
     */
    public static ResponseEntity<String> sendGetRequestWithoutParam(String url) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(url, String.class);
    }
}
