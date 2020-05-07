package com.webank.wecube.platform.gateway.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.gateway.dto.CommonResponseDto;
import com.webank.wecube.platform.gateway.dto.LoggerInfoDto;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/gateway/v1")
public class ApplicationInfomationController {


    @GetMapping("/appinfo/loggers/query")
    public Mono<CommonResponseDto> queryLoggers() {
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

        return Mono.just(CommonResponseDto.okayWithData(loggerInfos));
    }

    @PostMapping("/appinfo/loggers/update")
    public Mono<CommonResponseDto> changeLogLevel(@RequestBody LoggerInfoDto dto) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (!StringUtils.isEmpty(dto.getLevel())) {
            ch.qos.logback.classic.Logger logger = loggerContext.getLogger(dto.getPath());
            logger.setLevel(Level.toLevel(dto.getLevel()));
        }

        return Mono.just(CommonResponseDto.okay());
    }

}
