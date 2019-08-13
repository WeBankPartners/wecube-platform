package com.webank.wecube.core.controller;

import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.commons.ApplicationProperties;
import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import javax.naming.SizeLimitExceededException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static com.webank.wecube.core.domain.JsonResponse.okayWithData;
import static com.webank.wecube.core.domain.MenuItem.*;

@RestController
@Slf4j
@RequestMapping("/cmdb")
public class CmdbFileManagementController {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT })
    @PostMapping("/files/upload")
    @ResponseBody
    public JsonResponse uploadFile(@RequestParam(value = "file", required = false) MultipartFile file) throws SizeLimitExceededException {
        if (file.getSize() > applicationProperties.getMaxFileSize().toBytes()) {
            String errorMessage = String.format("Upload file failed due to file size (%s bytes) exceeded limitation (%s KB).", file.getSize(), applicationProperties.getMaxFileSize().toKilobytes());
            log.warn(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }
        return okayWithData(cmdbServiceV2Stub.uploadFile(file.getResource()));
    }

    @RolesAllowed({ MENU_ADMIN_CMDB_MODEL_MANAGEMENT, MENU_DESIGNING_CI_DATA_MANAGEMENT, MENU_DESIGNING_CI_DATA_ENQUIRY})
    @GetMapping("/files/{file-id}")
    public void downloadFile(@PathVariable(value = "file-id") int fileId, HttpServletResponse response) {
        if (fileId <= 0) throw new WecubeCoreException("Invalid file-id: " + fileId);
        try {
            ServletOutputStream out = response.getOutputStream();
            ResponseEntity<byte[]> responseEntity = cmdbServiceV2Stub.downloadFile(fileId);
            if (responseEntity == null) {
                throw new WecubeCoreException("File object not found in cmdb server for file-id: " + fileId);
            }
            response.setCharacterEncoding("utf-8");
            Optional<String> contentType = getContentType(responseEntity.getHeaders());
            contentType.ifPresent(response::setContentType);
            out.write(responseEntity.getBody());
            out.flush();
            out.close();
        } catch (Exception e) {
            String errorMessage = String.format("Failed to download file (fileId:%s) due to %s ", fileId, e.getMessage());
            log.warn(errorMessage, e);
            throw new WecubeCoreException(errorMessage);
        }
    }

    private Optional<String> getContentType(HttpHeaders httpHeaders){
        if (httpHeaders == null) return Optional.empty();
        MediaType contentType = httpHeaders.getContentType();
        if (contentType == null) return Optional.empty();
        return Optional.of(contentType.toString());
    }
}



