package com.webank.wecube.platform.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.PluginAsyncInvocationResultDto;

@RestController
@RequestMapping("/v1")
public class WorkflowProcessInvocationResultController {
    
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessInvocationResultController.class);

    
    @PostMapping("")
    public CommonResponseDto asyncInvocationResult(@RequestBody PluginAsyncInvocationResultDto requestDto){
        if(log.isInfoEnabled()){
            log.info("plugin invocation result:{}", requestDto);
        }
        
        
        
        return CommonResponseDto.okay();
    }
}
