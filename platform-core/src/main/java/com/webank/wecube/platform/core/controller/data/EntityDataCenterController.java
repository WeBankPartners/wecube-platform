package com.webank.wecube.platform.core.controller.data;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.data.EntityQuerySpecDto;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.service.data.EntityDataCenterService;

@RestController
@RequestMapping("/v1")
public class EntityDataCenterController {

    @Autowired
    private EntityDataCenterService entityDataCenterService;

    @PostMapping("/packages/{package-name}/entities/{entity-name}/query")
    public CommonResponseDto retrieveEntities(@PathVariable("package-name") String packageName,
            @PathVariable("entity-name") String entityName,
            @RequestParam(name = "procInstId", required = false) String procInstId,
            @RequestParam(name = "nodeInstId", required = false) String nodeInstId,
            @RequestBody EntityQuerySpecDto querySpecDto) {

        List<Map<String, Object>> entityDetails = entityDataCenterService.retieveEntities(packageName, entityName,
                querySpecDto, procInstId, nodeInstId);
        return CommonResponseDto.okayWithData(entityDetails);
    }

}
