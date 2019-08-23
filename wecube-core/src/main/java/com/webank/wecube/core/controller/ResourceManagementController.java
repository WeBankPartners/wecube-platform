package com.webank.wecube.core.controller;

import static com.webank.wecube.core.domain.JsonResponse.okay;
import static com.webank.wecube.core.domain.JsonResponse.okayWithData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.webank.wecube.core.commons.ApplicationProperties.CmdbDataProperties;
import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.dto.QueryRequest;
import com.webank.wecube.core.dto.ResourceItemDto;
import com.webank.wecube.core.dto.ResourceServerDto;
import com.webank.wecube.core.service.resource.ResourceAvaliableStatus;
import com.webank.wecube.core.service.resource.ResourceItemType;
import com.webank.wecube.core.service.resource.ResourceManagementService;
import com.webank.wecube.core.service.resource.ResourceServerType;

@RestController
@RequestMapping("/resource")
public class ResourceManagementController {

    @Autowired
    CmdbDataProperties cmdbDataProperties;

    @Autowired
    private ResourceManagementService resourceService;

    @PostMapping("/servers/retrieve")
    @ResponseBody
    public JsonResponse retrieveServers(@RequestBody QueryRequest queryRequest) {
        return okayWithData(resourceService.retrieveServers(queryRequest));
    }

    @PostMapping("/servers/create")
    @ResponseBody
    public JsonResponse createServers(@RequestBody List<ResourceServerDto> resourceServers) {
        return okayWithData(resourceService.createServers(resourceServers));
    }

    @PostMapping("/servers/update")
    @ResponseBody
    public JsonResponse updateServers(@RequestBody List<ResourceServerDto> resourceServers) {
        return okayWithData(resourceService.updateServers(resourceServers));
    }

    @PostMapping("/servers/delete")
    @ResponseBody
    public JsonResponse deleteServers(@RequestBody List<ResourceServerDto> resourceServers) {
        resourceService.deleteServers(resourceServers);
        return okay();
    }

    @PostMapping("/items/retrieve")
    @ResponseBody
    public JsonResponse retrieveItems(@RequestBody QueryRequest queryRequest) {
        return okayWithData(resourceService.retrieveItems(queryRequest));
    }

    @PostMapping("/items/create")
    @ResponseBody
    public JsonResponse createItems(@RequestBody List<ResourceItemDto> resourceItems) {
        return okayWithData(resourceService.createItems(resourceItems));
    }

    @PostMapping("/items/update")
    @ResponseBody
    public JsonResponse updateItems(@RequestBody List<ResourceItemDto> resourceItems) {
        return okayWithData(resourceService.updateItems(resourceItems));
    }

    @PostMapping("/items/delete")
    @ResponseBody
    public JsonResponse deleteItems(@RequestBody List<ResourceItemDto> resourceItems) {
        resourceService.deleteItems(resourceItems);
        return okay();
    }

    @GetMapping("/constants/resource-server-types")
    @ResponseBody
    public JsonResponse getResourceServerType() {
        List<String> resourceServerTypes = Lists.newLinkedList();
        for (ResourceServerType type : ResourceServerType.values()) {
            if (ResourceServerType.NONE.equals(type))
                continue;

            resourceServerTypes.add(type.getCode());
        }
        return okayWithData(resourceServerTypes);
    }

    @GetMapping("/constants/resource-item-types")
    @ResponseBody
    public JsonResponse getResourceItemType() {
        List<String> resourceItemTypes = Lists.newLinkedList();
        for (ResourceItemType type : ResourceItemType.values()) {
            if (ResourceItemType.NONE.equals(type))
                continue;

            resourceItemTypes.add(type.getCode());
        }
        return okayWithData(resourceItemTypes);
    }

    @GetMapping("/constants/resource-item-status")
    @ResponseBody
    public JsonResponse getResourceItemStatus() {
        List<String> resourceItemStatus = Lists.newLinkedList();
        for (ResourceAvaliableStatus type : ResourceAvaliableStatus.values()) {
            if (ResourceAvaliableStatus.NONE.equals(type))
                continue;

            resourceItemStatus.add(type.getCode());
        }
        return okayWithData(resourceItemStatus);
    }
}
