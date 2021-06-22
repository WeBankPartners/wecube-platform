package com.webank.wecube.platform.core.controller.workflow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.client.encryption.StringUtilsEx;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.workflow.InterfaceParameterDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoExportImportDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefOutlineDto;
import com.webank.wecube.platform.core.dto.workflow.ProcRoleRequestDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDataPreviewDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefBriefDto;
import com.webank.wecube.platform.core.service.workflow.ProcessRoleServiceImpl;
import com.webank.wecube.platform.core.service.workflow.WorkflowDataService;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcDefMigrationService;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcDefService;

@RestController
@RequestMapping("/v1")
public class WorkflowProcessDefinitionController {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessDefinitionController.class);

    @Autowired
    private WorkflowProcDefService procDefService;

    @Autowired
    private WorkflowProcDefMigrationService procDefMigrationService;

    @Autowired
    private WorkflowDataService workflowDataService;

    @Autowired
    private ProcessRoleServiceImpl processRoleService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/process/definitions/deploy")
    public CommonResponseDto deployProcessDefinition(@RequestBody ProcDefInfoDto requestDto, @RequestParam(value = "continue_token", required = false) String continueToken) {
        if (log.isDebugEnabled()) {
            log.debug("deploy process:procDefKey={},procDefName={},rootEntity={}, continueToken={}", requestDto.getProcDefKey(),
                    requestDto.getProcDefName(), requestDto.getRootEntity(), continueToken);
        }

        //TODO 
        //#2222
        ProcDefOutlineDto result = procDefService.deployProcessDefinition(requestDto, continueToken);
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
            @RequestParam(name = "includeDraft", required = false, defaultValue = "1") int includeDraft,
            @RequestParam(name = "permission", required = false, defaultValue = "") String permission) {
        boolean includeDraftProcDef = (includeDraft == 1);
        List<ProcDefInfoDto> result = procDefService.getProcessDefinitions(includeDraftProcDef, permission);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/definitions/{proc-def-id}/root-entities")
    public CommonResponseDto getProcessDefinitionRootEntities(@PathVariable("proc-def-id") String procDefId) {

        List<Map<String, Object>> result = workflowDataService.getProcessDefinitionRootEntities(procDefId);
        return CommonResponseDto.okayWithData(result);
    }
    
    @GetMapping("/process/definitions/process-keys/{proc-def-key}/root-entities")
    public CommonResponseDto getProcessDefinitionRootEntitiesByProcDefKey(@PathVariable("proc-def-key") String procDefKey) {

        List<Map<String, Object>> result = workflowDataService.getProcessDefinitionRootEntitiesByProcDefKey(procDefKey);
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
    public CommonResponseDto getProcessDataPreview(@PathVariable("proc-def-id") String procDefId,
            @PathVariable("entity-data-id") String dataId) {
        ProcessDataPreviewDto result = workflowDataService.generateProcessDataPreview(procDefId, dataId);
        return CommonResponseDto.okayWithData(result);
    }

    @GetMapping("/process/{proc-id}/roles")
    public CommonResponseDto retrieveProcRoleBinding(@PathVariable("proc-id") String procId) {
        return CommonResponseDto.okayWithData(processRoleService.retrieveRoleNamesByProcess(procId));
    }

    @PostMapping("/process/{proc-id}/roles")
    public CommonResponseDto updateProcRoleBinding(@PathVariable("proc-id") String procId,
            @RequestBody ProcRoleRequestDto procRoleRequestDto) {
        processRoleService.updateProcRoleBinding(procId, procRoleRequestDto);
        return CommonResponseDto.okay();
    }

    @DeleteMapping("/process/{proc-id}/roles")
    public CommonResponseDto deleteProcRoleBinding(@PathVariable("proc-id") String procId,
            @RequestBody ProcRoleRequestDto procRoleRequestDto) {
        processRoleService.deleteProcRoleBinding(procId, procRoleRequestDto);
        return CommonResponseDto.okay();
    }

    @GetMapping(value = "/process/definitions/{proc-def-id}/export", produces = { MediaType.ALL_VALUE })
    public ResponseEntity<byte[]> exportProcessDefinition(@PathVariable("proc-def-id") String procDefId) {

        ProcDefInfoExportImportDto result = procDefMigrationService.exportProcessDefinition(procDefId);
        String filename = assembleProcessExportFilename(result);

        String filedata = convertResult(result);
        byte[] filedataBytes = StringUtilsEx.encodeBase64String(filedata.getBytes(Charset.forName("utf-8")))
                .getBytes(Charset.forName("utf-8"));
        // ByteArrayResource resource = new ByteArrayResource(filedataBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add("Content-Disposition", String.format("attachment;filename=%s", filename));

        if (log.isInfoEnabled()) {
            log.info("finished export process definition,size={},filename={}", filedataBytes.length, filename);
        }
        return ResponseEntity.ok().headers(headers).contentLength(filedataBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(filedataBytes);
    }

    @PostMapping(value = "/process/definitions/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public CommonResponseDto importProcessDefinition(@RequestParam("uploadFile") MultipartFile file,
            HttpServletRequest request) {
        if (file == null || file.getSize() <= 0) {
            log.error("invalid file content uploaded");
            throw new WecubeCoreException("3128", "Invalid file content uploaded.");
        }

        if (log.isInfoEnabled()) {
            log.info("About to import process definition,filename={},size={}", file.getOriginalFilename(),
                    file.getSize());
        }

        try {
            String filedata = IOUtils.toString(file.getInputStream(), Charset.forName("utf-8"));
            String jsonData = new String(StringUtilsEx.decodeBase64(filedata), Charset.forName("utf-8"));
            ProcDefInfoExportImportDto importDto = convertImportData(jsonData);

            ProcDefInfoDto result = procDefMigrationService.importProcessDefinition(importDto);
            return CommonResponseDto.okayWithData(result);
        } catch (IOException e) {
            log.error("errors while reading upload file", e);
            throw new WecubeCoreException("3129", "Failed to import process definition.");
        }

    }

    private ProcDefInfoExportImportDto convertImportData(String jsonData) {
        ProcDefInfoExportImportDto dto = null;
        try {
            dto = objectMapper.readValue(jsonData, ProcDefInfoExportImportDto.class);
            return dto;
        } catch (IOException e) {
            log.error("errors while convert json data", e);
            return null;
        }
    }

    private String convertResult(ProcDefInfoExportImportDto result) {
        String content = "";
        try {
            content = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("errors while converting result", e);
            throw new WecubeCoreException("3130", "Failed to convert result.");
        }

        return content;

    }

    private String assembleProcessExportFilename(ProcDefInfoExportImportDto result) {
        String plainFilename = String.format("proc-%s-%s.pds", result.getProcDefName(), result.getProcDefKey());
        try {
            String encodedFilename = URLEncoder.encode(plainFilename, "UTF-8");
            return encodedFilename;
        } catch (UnsupportedEncodingException e) {
            log.error("encoding error", e);
            return "filename.pds";
        }
    }

}
