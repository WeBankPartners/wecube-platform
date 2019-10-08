package com.webank.wecube.platform.auth.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;

/**
 * 
 * @author gavin
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonResponseDto handleException(Exception e) {
        String errMsg = String.format("Proccessing failed cause by %s:%s", e.getClass().getSimpleName(),
                e.getMessage() == null ? "" : e.getMessage());
        log.error(errMsg+"\n", e);
        return CommonResponseDto.error(errMsg);
    }

}
