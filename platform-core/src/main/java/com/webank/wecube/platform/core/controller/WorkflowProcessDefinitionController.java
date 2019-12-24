package com.webank.wecube.platform.core.controller;

import java.nio.charset.Charset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.client.encryption.StringUtilsEx;
import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.GraphNodeDto;
import com.webank.wecube.platform.core.dto.workflow.InterfaceParameterDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoExportImportDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.service.workflow.ProcessRoleServiceImpl;
import com.webank.wecube.platform.core.service.workflow.WorkflowDataService;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcDefService;

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

    private ObjectMapper objectMapper = new ObjectMapper();

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
    public CommonResponseDto getProcessDefinitions(@RequestHeader(value = "Authorization") String token,
            @RequestParam(name = "includeDraft", required = false, defaultValue = "1") int includeDraft,
            @RequestParam(name = "permission", required = false, defaultValue = "") String permission) {
        log.info("currentUser:{}", AuthenticationContextHolder.getCurrentUsername());
        boolean includeDraftProcDef = (includeDraft == 1);
        List<ProcDefInfoDto> result = procDefService.getProcessDefinitions(token, includeDraftProcDef, permission);
        return CommonResponseDto.okayWithData(result);
    }

    @DeleteMapping("/process/definitions/{proc-def-id}")
    public CommonResponseDto removeProcessDefinition(@RequestHeader("Authorization") String token,
            @PathVariable("proc-def-id") String procDefId) {
        procDefService.removeProcessDefinition(token, procDefId);
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
    public CommonResponseDto getProcessDataPreview(@PathVariable("proc-def-id") String procDefId,
            @PathVariable("entity-data-id") String dataId) {
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
            @PathVariable("proc-id") String procId, @RequestBody ProcRoleRequestDto procRoleRequestDto) {
        try {
            processRoleService.updateProcRoleBinding(token, procId, procRoleRequestDto);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
        return CommonResponseDto.okay();
    }

    @DeleteMapping("/process/{proc-id}/roles")
    public CommonResponseDto deleteProcRoleBinding(@RequestHeader(value = "Authorization") String token,
            @PathVariable("proc-id") String procId, @RequestBody ProcRoleRequestDto procRoleRequestDto) {
        try {
            processRoleService.deleteProcRoleBinding(token, procId, procRoleRequestDto);
        } catch (WecubeCoreException ex) {
            return CommonResponseDto.error(ex.getMessage());
        }
        return CommonResponseDto.okay();
    }

    @GetMapping(value = "/process/definitions/{proc-def-id}/export", produces = { MediaType.ALL_VALUE })
    public ResponseEntity<byte[]> exportProcessDefinition(@PathVariable("proc-def-id") String procDefId) {
        log.info("=== export ===");

        ProcDefInfoExportImportDto result = procDefService.exportProcessDefinition(procDefId);
        String filename = assembleProcessExportFilename(result);

        String filedata = convertResult(result);
        byte[] filedataBytes = StringUtilsEx.encodeBase64String(filedata.getBytes(Charset.forName("utf-8")))
                .getBytes(Charset.forName("utf-8"));
        log.info("filedata:{}", filedata);
        log.info("string len:{}", filedata.length());
        log.info("byte len:{}", filedataBytes.length);
        // ByteArrayResource resource = new ByteArrayResource(filedataBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add("Content-Disposition", String.format("attachment;filename=%s", filename));
        return ResponseEntity.ok().headers(headers).contentLength(filedataBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(filedataBytes);
    }

    @PostMapping(value = "/process/definitions/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public CommonResponseDto importProcessDefinition(@RequestParam("uploadFile") MultipartFile file,
            @RequestParam(value = "filename", required = false) String filename) {
        log.info("=== import === {}", filename);
        if (file != null) {
            log.info("file size:{}", file.getSize());
            log.info("filename:{}", file.getName());
        }
        return CommonResponseDto.okay();
    }

    private String convertResult(ProcDefInfoExportImportDto result) {
        String content = "";
        try {
            content = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("errors while converting result", e);
            throw new WecubeCoreException("Failed to convert result.");
        }

        return content;

    }

    private String assembleProcessExportFilename(ProcDefInfoExportImportDto result) {
        return String.format("proc-%s-%s.pds", result.getProcDefName(), result.getProcDefKey());
    }

}
