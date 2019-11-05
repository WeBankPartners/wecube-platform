package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.domain.JsonResponse.okayWithData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.domain.JsonResponse;
import com.webank.wecube.platform.core.dto.SqlQueryRequest;
import com.webank.wecube.platform.core.service.resource.ResourceDataQueryService;

@RestController()
@RequestMapping("/packages/{package_id}/resources")
public class ResourceDataController {
    private Logger logger = LoggerFactory.getLogger(ResourceDataController.class);
    
    @Autowired
    private ResourceDataQueryService resourceDataQueryService;
    
    @PostMapping("/mysql/query")
    @ResponseBody
    public JsonResponse queryDB(@PathVariable("package_id")int packageId, @RequestBody SqlQueryRequest sqlQueryRequest){
        return okayWithData(resourceDataQueryService.queryDB(packageId, sqlQueryRequest));
    }

    @GetMapping("/s3/files")
    @ResponseBody
    public JsonResponse queryS3Files(@PathVariable("package_id")int packageId){
        return okayWithData(resourceDataQueryService.queryS3Files(packageId));
    }
    
    
}
