package com.webank.wecube.platform.core.support.gateway;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.commons.ApplicationProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GatewayServiceStub {
    private static final String REGISTER_ROUTE_ITEMS = "/gateway/v1/route-items";
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationProperties applicationProperties;

    public GatewayResponse registerRoute(RegisterRouteItemsDto requestBody) {
        String targetUrl = "http://" + applicationProperties.getGatewayUrl() + REGISTER_ROUTE_ITEMS;
        log.info("About to POST {} to GATEWAY with requestBody {}", targetUrl, requestBody);
        GatewayResponse response = restTemplate.postForObject(REGISTER_ROUTE_ITEMS, requestBody, GatewayResponse.class);
        log.info("GATEWAY response: {} ", response);
        return response;
    }
}
