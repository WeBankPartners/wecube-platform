package com.webank.wecube.platform.core.controller.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.PluginAsyncInvocationResultDto;
import com.webank.wecube.platform.core.service.workflow.AsyncPluginInvocationService;

@RestController
@RequestMapping("/v1")
public class WorkflowProcessInvocationResultController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessInvocationResultController.class);

    @Autowired
    private AsyncPluginInvocationService workflowProcAsyncInvocationService;

    @PostMapping("/process/instances/callback")
    public CommonResponseDto asyncInvocationResult(@RequestBody PluginAsyncInvocationResultDto asyncResultDto) {
        if (log.isInfoEnabled()) {
            log.info("About to process asynchnorous invocation result request:{}", asyncResultDto);
        }

        workflowProcAsyncInvocationService.handleAsyncInvocationResult(asyncResultDto);

        return CommonResponseDto.okay();
    }
}
