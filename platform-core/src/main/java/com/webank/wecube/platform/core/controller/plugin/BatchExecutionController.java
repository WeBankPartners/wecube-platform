package com.webank.wecube.platform.core.controller.plugin;

import static com.webank.wecube.platform.core.dto.plugin.CommonResponseDto.okayWithData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.BatchExecutionRequestDto;
import com.webank.wecube.platform.core.dto.plugin.BatchExecutionResultDto;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.ItsDangerConfirmResultDto;
import com.webank.wecube.platform.core.service.plugin.BatchExecutionService;

@RestController
@RequestMapping("/v1")
public class BatchExecutionController {

    @Autowired
    private BatchExecutionService batchExecutionService;

    /**
     * 
     * @param batchExecutionRequest
     * @return
     */
    @PostMapping("/batch-execution/run")
    public CommonResponseDto runBatchExecution(@RequestBody BatchExecutionRequestDto batchExecutionRequest,
            @RequestParam(value = "continue_token", required = false) String continueToken) {

        BatchExecutionResultDto result = batchExecutionService.handleBatchExecutionJob(batchExecutionRequest,
                continueToken);
        ItsDangerConfirmResultDto itsResultDto = result.getItsDangerConfirmResultDto();
        if (itsResultDto != null) {
            CommonResponseDto retDto = new CommonResponseDto();
            retDto.setData(itsResultDto.getData());
            retDto.setMessage(itsResultDto.getMessage());
            retDto.setStatus(itsResultDto.getStatus());
            return retDto;
        } else {

            CommonResponseDto retObject = okayWithData(result.getResult());
            return retObject;
        }
        // return
        // okayWithData(batchExecutionService.handleBatchExecutionJob(batchExecutionRequest));
    }

}
