package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.SqlQueryRequest;
import com.webank.wecube.platform.core.service.resource.ResourceDataQueryService;

@RestController()
@RequestMapping("/v1/packages/{package_id}/resources")
public class ResourceDataController {
    
    @Autowired
    private ResourceDataQueryService resourceDataQueryService;
    
    @PostMapping("/mysql/query")
    public CommonResponseDto queryDB(@PathVariable("package_id") String packageId, @RequestBody SqlQueryRequest sqlQueryRequest){
        return okayWithData(resourceDataQueryService.queryDB(packageId, sqlQueryRequest));
    }

    @GetMapping("/s3/files")
    public CommonResponseDto queryS3Files(@PathVariable("package_id") String packageId){
        return okayWithData(resourceDataQueryService.queryS3Files(packageId));
    }
    
    
}
