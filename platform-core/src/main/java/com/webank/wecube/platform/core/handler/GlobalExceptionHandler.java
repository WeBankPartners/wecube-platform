package com.webank.wecube.platform.core.handler;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.CommonResponseDto;

/**
 * 
 * @author gavin
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WecubeCoreException.class)
    @ResponseBody
    public CommonResponseDto handleWecubeException(WecubeCoreException e) {
        String errMsg = String.format("Proccessing failed cause by %s:%s", e.getClass().getSimpleName(),
                e.getMessage() == null ? "" : e.getMessage());
        log.error(errMsg+"\n", e);
        return CommonResponseDto.error(e.getMessage());
    }
    
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public CommonResponseDto defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        log.error("errors ocurred:", e);
        log.error("---DefaultException Handler---Host {} invokes url {} ERROR: {}", req.getRemoteHost(), req.getRequestURL(), e.getMessage());
        return CommonResponseDto.error(e.getMessage());
    }

}
