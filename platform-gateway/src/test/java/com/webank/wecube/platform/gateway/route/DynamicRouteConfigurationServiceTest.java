package com.webank.wecube.platform.gateway.route;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import com.webank.wecube.platform.gateway.dto.CommonResponseDto;

import reactor.core.publisher.Mono;

public class DynamicRouteConfigurationServiceTest {
    
    String accessKey = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJXRUNVQkUtQ09SRSIsImlhdCI6MTU3MDY5MDMwMCwidHlwZSI6ImFjY2Vzc1Rva2VuIiwiY2xpZW50VHlwZSI6IlNVQl9TWVNURU0iLCJleHAiOjE2MDIzMTI3MDAsImF1dGhvcml0eSI6IltTVUJfU1lTVEVNXSJ9.Mq8g_ZoPIQ_mB59zEq0KVtwGn_uPqL8qn6sP7WzEiJxoXQQIcVe7mYsG-E2jxCShEQL7PsMNLM47MYuY7R5nBg";
    String baseUrl = "http://localhost:10086/mock";
    String uri = "/v1/api/data/user";
    
    Logger log = LoggerFactory.getLogger(getClass());

    @Ignore
    @Test
    public void testLoadRoutes() {
        WebClient webClient = WebClient.create(baseUrl);
        Mono<CommonResponseDto> result = webClient.get().uri(uri).headers(headers -> headers.setBearerAuth(accessKey)).retrieve().bodyToMono(CommonResponseDto.class);
        Assert.assertNotNull(result);
        CommonResponseDto dto = result.block();
        Assert.assertNotNull(dto);
        
        log.info("dto:{} {}", dto.getStatus(), dto.getData());
    }

}
