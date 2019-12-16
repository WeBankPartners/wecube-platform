package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.*;
import com.webank.wecube.platform.core.service.workflow.ProcessRoleServiceImpl;
import com.webank.wecube.platform.core.service.workflow.WorkflowDataService;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcDefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class WorkflowProcessDefinitionController {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessDefinitionController.class);

    @Autowired
    private WorkflowProcDefService procDefService;

    @Autowired
    private WorkflowDataService workflowDataService;

    @Autowired
    private ProcessRoleServiceImpl processRoleService;

    @PostMapping("/process/definitions/deploy")
    public CommonResponseDto deployProcessDefinition(@RequestBody ProcDefInfoDto requestDto) {
        if (log.isDebugEnabled()) {
            log.debug("deploy process:procDefKey={},procDefName={},rootEntity={}", requestDto.getProcDefKey(),
                    requestDto.getProcDefName(), requestDto.getRootEntity());
        }


        ProcDefOutlineDto result = procDefService.deployProcessDefinition(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    @PostMapping("/process/definitions/draft")
    public CommonResponseDto draftProcessDefinition(@RequestBody ProcDefInfoDto requestDto) {
        if (log.isDebugEnabled()) {
            log.debug("draft process:procDefKey={},procDefName={},rootEntity={}", requestDto.getProcDefKey(),
                    requestDto.getProcDefName(), requestDto.getRootEntity());
        }

        ProcDefInfoDto result = procDefService.draftProcessDefinition(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions")
    public CommonResponseDto getProcessDefinitions(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam(name = "includeDraft", required = false, defaultValue = "1") int includeDraft,
            @RequestParam(name = "permission", required = false, defaultValue = "") String permission) {
        log.info("currentUser:{}", AuthenticationContextHolder.getCurrentUsername());
        boolean includeDraftProcDef = (includeDraft == 1);
        List<ProcDefInfoDto> result = procDefService.getProcessDefinitions(token, includeDraftProcDef, permission);
        return CommonResponseDto.okayWithData(result);
    }

    @DeleteMapping("/process/definitions/{proc-def-id}")
    public CommonResponseDto removeProcessDefinition(@PathVariable("proc-def-id") String procDefId) {
        procDefService.removeProcessDefinition(procDefId);
        return CommonResponseDto.okay();
    }

    @GetMapping("/process/definitions/{id}/detail")
    public CommonResponseDto getProcessDefinition(@PathVariable(name = "id") String id) {
        ProcDefInfoDto result = procDefService.getProcessDefinition(id);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{id}/outline")
    public CommonResponseDto getProcessDefinitionOutline(@PathVariable(name = "id") String id) {
        ProcDefOutlineDto result = procDefService.getProcessDefinitionOutline(id);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{proc-def-id}/tasknodes/briefs")
    public CommonResponseDto getTaskNodeBriefs(@PathVariable("proc-def-id") String procDefId) {
        List<TaskNodeDefBriefDto> result = procDefService.getTaskNodeBriefs(procDefId);

        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{proc-def-id}/tasknodes/{node-def-id}")
    public CommonResponseDto getTaskNodeParameters(@PathVariable("proc-def-id") String procDefId,
                                                   @PathVariable("node-def-id") String nodeDefId) {
        List<InterfaceParameterDto> result = workflowDataService.getTaskNodeParameters(procDefId, nodeDefId);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{proc-def-id}/preview/entities/{entity-data-id}")
    public CommonResponseDto getProcessDataPreview(@PathVariable("proc-def-id") String procDefId, @PathVariable("entity-data-id") String dataId) {
        List<GraphNodeDto> result = workflowDataService.getProcessDataPreview(procDefId, dataId);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/{proc-id}/roles")
    public CommonResponseDto retrieveProcRoleBinding(@RequestHeader(value = "Authorization") String token,
                                                     @PathVariable("proc-id") String procId) {
        try {
            return CommonResponseDto.okayWithData(processRoleService.retrieveRoleIdByProcId(token, procId));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @PostMapping("/process/{proc-id}/roles")
    public CommonResponseDto updateProcRoleBinding(@RequestHeader(value = "Authorization") String token,
                                                   @PathVariable("proc-id") String procId,
                                                   @RequestBody ProcRoleRequestDto procRoleRequestDto) {
        try {
            return CommonResponseDto.okayWithData(processRoleService.updateProcRoleBinding(token, procId, procRoleRequestDto));
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
    }

    @DeleteMapping("/process/{proc-id}/roles")
    public CommonResponseDto deleteProcRoleBinding(@RequestHeader(value = "Authorization") String token,
                                                   @PathVariable("proc-id") String procId,
                                                   @RequestBody ProcRoleRequestDto procRoleRequestDto) {
        try {
            processRoleService.deleteProcRoleBinding(token, procId, procRoleRequestDto);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
        return CommonResponseDto.okay();
    }

}
