package com.webank.wecube.platform.core.exception;

import static com.webank.wecube.platform.core.domain.JsonResponse.error;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.JsonResponse;

//@RestController
//@ControllerAdvice
public class WecubeExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(WecubeExceptionHandler.class);

    @ExceptionHandler(value = WecubeCoreException.class)
    @ResponseBody
    public JsonResponse baseErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        logger.error("---WecubeCoreException Handler---Host {} invokes url {} ERROR: {}", req.getRemoteHost(), req.getRequestURL(), e.getMessage());
        return error(e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonResponse defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        logger.error("errors ocurred:", e);
        logger.error("---DefaultException Handler---Host {} invokes url {} ERROR: {}", req.getRemoteHost(), req.getRequestURL(), e.getMessage());
        return error(e.getMessage());
    }
}