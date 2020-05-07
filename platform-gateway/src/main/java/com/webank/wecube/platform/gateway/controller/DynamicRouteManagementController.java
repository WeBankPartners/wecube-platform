package com.webank.wecube.platform.gateway.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.gateway.dto.CommonResponseDto;
import com.webank.wecube.platform.gateway.dto.MvcContextRouteConfigDto;
import com.webank.wecube.platform.gateway.dto.RouteItemInfoDto;
import com.webank.wecube.platform.gateway.route.DynamicRouteConfigurationService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/gateway/v1")
public class DynamicRouteManagementController {

    private static final Logger log = LoggerFactory.getLogger(DynamicRouteManagementController.class);

    @Autowired
    private DynamicRouteConfigurationService service;

    
    @GetMapping("/route-items")
    public Mono<CommonResponseDto> listRouteItems() {

        List<RouteItemInfoDto> items = service.listAllContextRouteItems();
        return Mono.just(CommonResponseDto.okayWithData(items));
    }

    @GetMapping("/loaded-routes")
    public Mono<CommonResponseDto> listLoadedRouteItems() {

        List<MvcContextRouteConfigDto> items = service.getAllMvcContextRouteConfigs();
        return Mono.just(CommonResponseDto.okayWithData(items));
    }

    @DeleteMapping("/route-items/{route-name}")
    public Mono<CommonResponseDto> deleteRouteItems(@PathVariable("route-name") String routeName) {
        log.info("to delete route {}", routeName);
        service.deleteRouteItem(routeName);
        return Mono.just(CommonResponseDto.okay());
    }
}
