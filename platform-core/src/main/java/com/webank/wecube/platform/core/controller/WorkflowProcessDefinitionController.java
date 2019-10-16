package com.webank.wecube.platform.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDefinitionDeployRequestDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDraftRequestDto;

@RestController
@RequestMapping("/v1/api")
public class WorkflowProcessDefinitionController {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessDefinitionController.class);

    @PostMapping("/process/definitions")
    public CommonResponseDto deployProcessDefinition(@RequestBody ProcessDefinitionDeployRequestDto deployRequest) {
        return null;
    }

    @GetMapping("/process/definitions")
    public CommonResponseDto getProcessDefinitions(
            @RequestParam(name = "includeDraft", required = false, defaultValue = "0") int includeDraft) {
        return null;
    }

    @GetMapping("/process/definitions/{process-id}")
    public CommonResponseDto getProcessDefinition(@PathVariable(name = "process-id") String processId) {
        return null;
    }

    @PutMapping("/process/definitions/{process-id}/task-nodes")
    public CommonResponseDto configureTaskNode(@PathVariable(name = "process-id") String processId,
            @RequestBody TaskNodeDraftRequestDto draftRequest) {
        return null;
    }

}
