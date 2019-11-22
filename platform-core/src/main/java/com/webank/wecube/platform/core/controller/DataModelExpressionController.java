package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.service.DataModelExpressionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class DataModelExpressionController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataModelExpressionServiceImpl dataModelExpressionService;
    
    @GetMapping("/dme/target-entity")
    @ResponseBody
    public JsonResponse getRefByIdInfoByPackageNameAndEntityName(
            @RequestParam(value = "package") String packageName,
            @RequestParam(value = "entity") String entityName
    ) {
        return JsonResponse.okayWithData(dataModelExpressionService.targetEntityQuery(packageName, entityName));
    }

}
