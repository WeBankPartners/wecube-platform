package com.webank.wecube.platform.core.utils;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpClientUtils {

    /**
     * Send get request to url with params
     *
     * @param url     target url
     * @param params  parameters
     * @param headers request headers
     * @return String
     */
    public static ResponseEntity<String> sendGetRequestWithParamMap(String url, MultiValueMap<String, String> params, HttpHeaders headers) {
        RestTemplate client = new RestTemplate();

        HttpMethod method = HttpMethod.GET;
        // set content type as form
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // combine param map and headers and one request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
        // send request and exchange the response to target class
        return client.exchange(url, method, requestEntity, String.class);
    }

    /**
     * Send get request to url without params
     *
     * @param url target url
     * @return String
     */
    public static ResponseEntity<String> sendGetRequestWithoutParam(String url) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity(url, String.class);
    }
}
