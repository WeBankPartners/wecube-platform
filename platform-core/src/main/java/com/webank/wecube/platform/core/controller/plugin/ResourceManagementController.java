package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okay;
import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.QueryRequestDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceItemDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceServerDto;
import com.webank.wecube.platform.core.dto.plugin.ResourceServerProductSerialDto;
import com.webank.wecube.platform.core.service.resource.ResourceItemStatus;
import com.webank.wecube.platform.core.service.resource.ResourceItemType;
import com.webank.wecube.platform.core.service.resource.ResourceManagementService;
import com.webank.wecube.platform.core.service.resource.ResourceServerStatus;
import com.webank.wecube.platform.core.service.resource.ResourceServerType;

@RestController
@RequestMapping("/resource")
public class ResourceManagementController {

    @Autowired
    private ResourceManagementService resourceService;
    
    @GetMapping("/servers/{id}/product-serial")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto retrieveResourceServerProductSerial(String resourceServerId) {
        ResourceServerProductSerialDto dto = resourceService.retrieveResourceServerProductSerial(resourceServerId);
        return okayWithData(dto);
    }

    @PostMapping("/servers/retrieve")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto retrieveServers(@RequestBody QueryRequestDto queryRequest) {
        return okayWithData(resourceService.retrieveServers(queryRequest));
    }

    @PostMapping("/servers/create")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto createServers(@RequestBody List<ResourceServerDto> resourceServers) {
        return okayWithData(resourceService.createServers(resourceServers));
    }

    @PostMapping("/servers/update")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto updateServers(@RequestBody List<ResourceServerDto> resourceServers) {
        return okayWithData(resourceService.updateServers(resourceServers));
    }

    /**
     * 
     * @param resourceServers
     * @return
     */
    @PostMapping("/servers/delete")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto deleteServers(@RequestBody List<ResourceServerDto> resourceServers) {
        resourceService.deleteServers(resourceServers);
        return okay();
    }

    /**
     * 
     * @param queryRequest
     * @return
     */
    @PostMapping("/items/retrieve")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto retrieveItems(@RequestBody QueryRequestDto queryRequest) {
        return okayWithData(resourceService.retrieveItems(queryRequest));
    }

    /**
     * 
     * @param resourceItems
     * @return
     */
    @PostMapping("/items/create")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto createItems(@RequestBody List<ResourceItemDto> resourceItems) {
        return okayWithData(resourceService.createItems(resourceItems));
    }

    /**
     * Allowed menu:ADMIN_RESOURCES_MANAGEMENT
     * @param resourceItems
     * @return
     */
    @PostMapping("/items/update")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto updateItems(@RequestBody List<ResourceItemDto> resourceItems) {
        return okayWithData(resourceService.updateItems(resourceItems));
    }

    /**
     * 
     * @param resourceItems
     * @return
     */
    @PostMapping("/items/delete")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto deleteItems(@RequestBody List<ResourceItemDto> resourceItems) {
        resourceService.deleteItems(resourceItems);
        return okay();
    }

    /**
     * 
     * @return
     */
    @GetMapping("/constants/resource-server-types")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto getResourceServerType() {
        List<String> resourceServerTypes = Lists.newLinkedList();
        for (ResourceServerType type : ResourceServerType.values()) {
            if (ResourceServerType.NONE.equals(type))
                continue;

            resourceServerTypes.add(type.getCode());
        }
        return okayWithData(resourceServerTypes);
    }

    /**
     * 
     * @return
     */
    @GetMapping("/constants/resource-item-types")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto getResourceItemType() {
        List<String> resourceItemTypes = Lists.newLinkedList();
        for (ResourceItemType type : ResourceItemType.values()) {
            if (ResourceItemType.NONE.equals(type))
                continue;

            resourceItemTypes.add(type.getCode());
        }
        return okayWithData(resourceItemTypes);
    }

    /**
     * 
     * @return
     */
    @GetMapping("/constants/resource-server-status")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto getResourceServerStatus() {
        List<String> resourceServerStatus = Lists.newLinkedList();
        for (ResourceServerStatus type : ResourceServerStatus.values()) {
            if (ResourceServerStatus.NONE.equals(type))
                continue;

            resourceServerStatus.add(type.getCode());
        }
        return okayWithData(resourceServerStatus);
    }

    /**
     * 
     * @return
     */
    @GetMapping("/constants/resource-item-status")
    @PreAuthorize("hasAnyAuthority('ADMIN_RESOURCES_MANAGEMENT','SUB_SYSTEM')")
    public CommonResponseDto getResourceItemStatus() {
        List<String> resourceItemStatus = Lists.newLinkedList();
        for (ResourceItemStatus type : ResourceItemStatus.values()) {
            if (ResourceItemStatus.NONE.equals(type))
                continue;

            resourceItemStatus.add(type.getCode());
        }
        return okayWithData(resourceItemStatus);
    }
}
