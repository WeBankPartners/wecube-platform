package com.webank.wecube.platform.gateway.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.gateway.dto.CommonResponseDto;
import com.webank.wecube.platform.gateway.dto.MvcContextRouteConfigDto;
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

    @GetMapping("/route-items")
    public Mono<CommonResponseDto> listRouteItems(){
        
        List<RouteItemInfoDto> items = service.listAllContextRouteItems();
        return Mono.just(CommonResponseDto.okayWithData(items));
    }
    
    @GetMapping("/loaded-routes")
    public Mono<CommonResponseDto> listLoadedRouteItems(){
        
        List<MvcContextRouteConfigDto> items = service.getAllMvcContextRouteConfigs();
        return Mono.just(CommonResponseDto.okayWithData(items));
    }
    
    @DeleteMapping("/route-items/{route-name}")
    public Mono<CommonResponseDto> deleteRouteItems(@PathVariable("route-name")String routeName){
        service.deleteRouteItem(routeName);
        return Mono.just(CommonResponseDto.okay());
    }

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
