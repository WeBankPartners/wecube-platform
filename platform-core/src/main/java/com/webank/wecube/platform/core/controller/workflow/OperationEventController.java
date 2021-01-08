package com.webank.wecube.platform.core.controller.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.event.OperationEventDto;
import com.webank.wecube.platform.core.dto.event.OperationEventResultDto;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.service.event.OperationEventManagementService;

@RestController
@RequestMapping("/v1")
public class OperationEventController {
    private static final Logger log = LoggerFactory.getLogger(OperationEventController.class);

    @Autowired
    private OperationEventManagementService operationEventManagementService;

    @PostMapping("/operation-events")
    public CommonResponseDto reportOperationEvent(@RequestBody OperationEventDto eventDto) {
        if (log.isInfoEnabled()) {
            log.info("About reporting operation event:{}", eventDto);
        }
        OperationEventResultDto result = operationEventManagementService.reportOperationEvent(eventDto);

        if (log.isInfoEnabled()) {
            log.info("Finished reporting operation event:{}", eventDto);
        }
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/source-sub-systems/{sub-system-code}/operation-events/{event-seq-no}")
    public CommonResponseDto queryOperationEvent(@PathVariable String subSystemCode, @PathVariable String eventSeqNo) {
        return CommonResponseDto.okay();
    }
}
