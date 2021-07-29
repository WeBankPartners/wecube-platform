package com.webank.wecube.platform.core.controller.plugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.PluginCertificationDto;
import com.webank.wecube.platform.core.dto.plugin.PluginCertificationExportDto;
import com.webank.wecube.platform.core.service.plugin.PluginCertificationService;

@RestController
@RequestMapping("/v1")
public class PluginCertificationController {
    private static final Logger log = LoggerFactory.getLogger(PluginCertificationController.class);

    @Autowired
    private PluginCertificationService pluginCertificationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/plugin-certifications")
    public CommonResponseDto getAllPluginCertifications() {
        List<PluginCertificationDto> resultDtos = pluginCertificationService.getAllPluginCertifications();

        return CommonResponseDto.okayWithData(resultDtos);
    }

    @DeleteMapping("/plugin-certifications/{id}")
    public CommonResponseDto removePluginCertification(@PathVariable("id") String id) {
        log.info("About to remove plugin certification by ID:{}", id);
        pluginCertificationService.removePluginCertification(id);
        return CommonResponseDto.okay();
    }

    @GetMapping(value = "/plugin-certifications/{id}/export", produces = { MediaType.ALL_VALUE })
    public ResponseEntity<byte[]> exportPluginCertification(@PathVariable("id") String id) {
        log.info("About to export plugin certification of ID:{}", id);
        PluginCertificationExportDto exportDto = pluginCertificationService.exportPluginCertification(id);

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String sDate = df.format(new Date());
        String filename = String.format("%s-%s.WeLic", exportDto.getPlugin(), sDate);

        String jsonData = convertResultToString(exportDto);
        byte[] zippedByteData = zipStringToBytes(jsonData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add("Content-Disposition", String.format("attachment;filename=%s", filename));

        if (log.isInfoEnabled()) {
            log.info("finished export plugin certification,size={},filename={}", zippedByteData.length, filename);
        }
        return ResponseEntity.ok().headers(headers).contentLength(zippedByteData.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(zippedByteData);
    }

    @PostMapping(value = "/plugin-certifications/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public CommonResponseDto importPluginCertification(@RequestParam("uploadFile") MultipartFile file,
            HttpServletRequest request) {
        if (file == null || file.getSize() <= 0) {
            log.error("invalid file content uploaded");
            throw new WecubeCoreException("3128", "Invalid file content uploaded.");
        }

        if (log.isInfoEnabled()) {
            log.info("About to import plugin certification,filename={},size={}", file.getOriginalFilename(),
                    file.getSize());
        }

        try {
            byte[] filedata = IOUtils.toByteArray(file.getInputStream());
            String jsonData = unzipBytesToString(filedata);
            PluginCertificationExportDto pluginCertificationExportDto = convertStringToDto(jsonData);
            // read value from file
            PluginCertificationDto resultDto = pluginCertificationService
                    .importPluginCertification(pluginCertificationExportDto);
            return CommonResponseDto.okayWithData(resultDto);
        } catch (IOException e) {
            log.error("errors while reading upload file", e);
            throw new WecubeCoreException("Failed to import plugin certification:" + e.getMessage());
        }

    }

    private byte[] zipStringToBytes(String content) {
        byte[] data = content.getBytes(Charset.forName("UTF-8"));
        byte[] output = new byte[0];

        Deflater compresser = new Deflater();

        ByteArrayOutputStream bos = null;
        try {
            compresser.reset();
            compresser.setInput(data);
            compresser.finish();
            bos = new ByteArrayOutputStream(data.length);
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            log.error("Errors while zip data.", e);
            throw new WecubeCoreException("Failed to zip data:" + e.getMessage());
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                log.info("errors while closing.", e);
            }
        }
        compresser.end();
        return output;
    }

    private String unzipBytesToString(byte[] byteData) {
        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(byteData);

        ByteArrayOutputStream o = new ByteArrayOutputStream(byteData.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = byteData;
            log.error("Failed to unzip byte data.", e);
            throw new WecubeCoreException("Failed to unzip data:" + e.getMessage());
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                log.info("errors while closing.", e);
            }
        }

        decompresser.end();
        return new String(output, Charset.forName("UTF-8"));
    }

    private PluginCertificationExportDto convertStringToDto(String content) {
        try {
            PluginCertificationExportDto dto = objectMapper.readValue(content, PluginCertificationExportDto.class);
            return dto;
        } catch (Exception e) {
            log.error("Failed to read json value.", e);
            throw new WecubeCoreException("Failed to import plugin certification:" + e.getMessage());
        }
    }

    private String convertResultToString(PluginCertificationExportDto dto) {
        String content = "";
        try {
            content = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.error("errors while converting result", e);
            throw new WecubeCoreException("3130", "Failed to convert result.");
        }

        return content;

    }
}
