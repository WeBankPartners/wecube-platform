package com.webank.wecube.platform.core.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDefinitionInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeInfoDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcessDefinitionService;

@RestController
@RequestMapping("/v1/api")
public class WorkflowProcessDefinitionController {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessDefinitionController.class);

    @Autowired
    private WorkflowProcessDefinitionService procDefService;

    @PostMapping("/process/definitions/deploy")
    public CommonResponseDto deployProcessDefinition(@RequestBody ProcessDefinitionInfoDto requestDto) {
        if (log.isDebugEnabled()) {
            log.debug("deploy process:procDefKey={},procDefName={},rootEntity={}", requestDto.getProcDefKey(),
                    requestDto.getProcDefName(),requestDto.getRootEntity());
        }
        
        ProcessDefinitionInfoDto result = procDefService.deployProcessDefinition(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    @PostMapping("/process/definitions/draft")
    public CommonResponseDto draftProcessDefinition(@RequestBody ProcessDefinitionInfoDto requestDto) {
        return null;
    }

    @GetMapping("/process/definitions")
    public CommonResponseDto getProcessDefinitions(
            @RequestParam(name = "includeDraft", required = false, defaultValue = "0") int includeDraft) {
        return null;
    }

    @GetMapping("/process/definitions/{id}")
    public CommonResponseDto getProcessDefinition(@PathVariable(name = "id") String id) {
        return null;
    }

    // @PutMapping("/process/definitions/{process-def-key}/task-nodes")
    // public CommonResponseDto configureTaskNode(@PathVariable(name =
    // "process-def-key") String processDefKey,
    // @RequestBody TaskNodeInfoDto request) {
    // if (log.isDebugEnabled()) {
    // log.debug("configure task node with process id:{},task node info:{}",
    // processId, request);
    // }
    //
    // if (StringUtils.isBlank(processId)) {
    // throw new WecubeCoreException("process id is blank.");
    // }
    //
    // TaskNodeInfoDto result = procDefService.configureTaskNode(processId,
    // request);
    //
    // return CommonResponseDto.okayWithData(result);
    // }

    @GetMapping("/process/definitions/{process-id}/task-nodes/{node-id}")
    public CommonResponseDto getTaskNodeInfo(@PathVariable(name = "process-id") String processId,
            @PathVariable(name = "node-id") String nodeId) {
        if (StringUtils.isBlank(processId) || StringUtils.isBlank(nodeId)) {
            throw new WecubeCoreException("process id or node id is blank.");
        }

        TaskNodeInfoDto result = procDefService.getTaskNodeInfo(processId, nodeId);

        return CommonResponseDto.okayWithData(result);
    }

}
