package com.webank.wecube.platform.core.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.LoggerInfoDto;
import com.webank.wecube.platform.core.service.ApplicationInformationService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

@RestController
@RequestMapping("/v1")
public class ApplicationInformationController {
	private static final Logger log = LoggerFactory.getLogger(ApplicationInformationController.class);
	
	@Autowired
	private ApplicationInformationService applicationInformationService;

	@GetMapping("/health-check")
	public ResponseEntity<CommonResponseDto> healthCheck() {
		try {
			applicationInformationService.healthCheck();
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(CommonResponseDto.okay());
		} catch (Exception e) {
			log.warn("Health checking failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
					.body(CommonResponseDto.error(e.getMessage()));
		}
	}
	
	@GetMapping("/appinfo/loggers/query")
    public CommonResponseDto queryLoggers() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        List<ch.qos.logback.classic.Logger> loggers = loggerContext.getLoggerList();

        List<LoggerInfoDto> loggerInfos = loggers.stream().filter(lm -> {
            return lm.getLevel() != null;
        }).map(lm -> {
            LoggerInfoDto info = new LoggerInfoDto();
            info.setLevel(lm.getLevel() == null ? null : lm.getLevel().toString());
            info.setPath(lm.getName());

            return info;
        }).collect(Collectors.toList());

        return CommonResponseDto.okayWithData(loggerInfos);
    }

    @PostMapping("/appinfo/loggers/update")
    public CommonResponseDto changeLogLevel(@RequestBody LoggerInfoDto dto) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (!StringUtils.isEmpty(dto.getLevel())) {
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger(dto.getPath());
            logger.setLevel(Level.toLevel(dto.getLevel()));
        }

        return CommonResponseDto.okay();
    }
}
