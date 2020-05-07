package com.webank.wecube.platform.gateway.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.gateway.dto.CommonResponseDto;
import com.webank.wecube.platform.gateway.dto.RouteItemInfoDto;
import com.webank.wecube.platform.gateway.dto.RouteItemPushDto;
import com.webank.wecube.platform.gateway.route.DynamicRouteConfigurationService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/gateway/v1")
public class DynamicRouteItemController {

    private static final Logger log = LoggerFactory.getLogger(DynamicRouteItemController.class);

    @Autowired
    private DynamicRouteConfigurationService service;

    @PostMapping("/route-items")
    public Mono<CommonResponseDto> pushRouteItems(@RequestBody RouteItemPushDto request) {
        String context = request.getContext();
        List<RouteItemInfoDto> items = request.getItems();

        if (items == null) {
            items = new ArrayList<>();
        }

        log.info("try to push route items for {} {}", context, items.size());

        service.pushRouteItem(context, items);

        return Mono.just(CommonResponseDto.okay());
    }
}
