package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.dto.CommonResponseDto.okayWithData;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.dto.BatchExecutionResult;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.service.BatchExecutionService;

@RestController
@RequestMapping("/v1")
public class BatchExecutionController {

    @Autowired
    private BatchExecutionService batchExecutionService;

    @PostMapping("/batch-execution/run")
    @ResponseBody
    public Object runBatchExecution(@RequestBody BatchExecutionRequestDto batchExecutionRequest) throws IOException {

        BatchExecutionResult result = batchExecutionService.handleBatchExecutionJob(batchExecutionRequest);
        if (result.getItsDangerConfirmResultDto() != null) {
            return result.getItsDangerConfirmResultDto();
        } else {

            CommonResponseDto retObject = okayWithData(result.getResult());
            return retObject;
        }
    }

}
