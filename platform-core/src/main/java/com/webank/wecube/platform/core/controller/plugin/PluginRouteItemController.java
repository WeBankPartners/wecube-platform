package com.webank.wecube.platform.core.controller.plugin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.PluginRouteItemDto;
import com.webank.wecube.platform.core.service.plugin.PluginRouteItemService;

@RestController
@RequestMapping("/v1")
public class PluginRouteItemController {
    private static final Logger log = LoggerFactory.getLogger(PluginRouteItemController.class);
            
    @Autowired
    private PluginRouteItemService pluginRouteItemService;

    @GetMapping("/route-items")
    public CommonResponseDto getAllPluginRouteItems() {
        if(log.isDebugEnabled()){
            log.debug("fetching all route item");
        }
        List<PluginRouteItemDto> routeItems = pluginRouteItemService.getAllPluginRouteItems();

        return CommonResponseDto.okayWithData(routeItems);
    }

    @GetMapping("/route-items/{name}")
    public CommonResponseDto getPluginRouteItemsByName(@PathVariable("name") String name) {
        if(log.isDebugEnabled()){
            log.debug("route item fetching for {}", name);
        }
        List<PluginRouteItemDto> routeItems = pluginRouteItemService.getPluginRouteItemsByName(name);

        return CommonResponseDto.okayWithData(routeItems);
    }
}
