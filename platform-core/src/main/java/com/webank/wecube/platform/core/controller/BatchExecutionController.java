package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.dto.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.plugin.BatchExecutionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.webank.wecube.platform.core.dto.CommonResponseDto.okayWithData;

import java.io.IOException;

@RestController
@RequestMapping("/v1")
public class BatchExecutionController {

    @Autowired
    private BatchExecutionService batchExecutionService;

    @PostMapping("/batch-execution/run")
    @ResponseBody
    public CommonResponseDto runBatchExecution(@RequestBody BatchExecutionRequestDto batchExecutionRequest)
            throws IOException {
        return okayWithData(batchExecutionService.handleBatchExecutionJob(batchExecutionRequest));
    }

}
