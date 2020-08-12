package com.webank.wecube.platform.auth.server.http;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.webank.wecube.platform.auth.server.common.AuthServerException;
import com.webank.wecube.platform.auth.server.dto.CommonResponseDto;

/**
 * 
 * @author gavin
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    public static final String MSG_ERR_CODE_PREFIX = "platform.auth.msg.errorcode.";

    public static final Locale DEF_LOCALE = Locale.ENGLISH;
    
    @Autowired
    private MessageSource messageSource;
    

    @ExceptionHandler(AuthServerException.class)
    @ResponseBody
    public CommonResponseDto handleAuthServerException(HttpServletRequest request, final Exception e,
            HttpServletResponse response) {
        String errMsg = String.format("Proccessing failed cause by %s:%s", e.getClass().getSimpleName(),
                e.getMessage() == null ? "" : e.getMessage());
        log.error(errMsg+"\n");
        
        AuthServerException wecubeError = (AuthServerException) e;
        
        return CommonResponseDto.error(determineI18nErrorMessage(request, wecubeError));
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonResponseDto handleException(Exception e) {
        String errMsg = String.format("Proccessing failed cause by %s:%s", e.getClass().getSimpleName(),
                e.getMessage() == null ? "" : e.getMessage());
        log.error(errMsg+"\n", e);
        return CommonResponseDto.error(errMsg);
    }
    
    private String determineI18nErrorMessage(HttpServletRequest request, AuthServerException e) {
        Locale locale = request.getLocale();
        if (locale == null) {
            locale = DEF_LOCALE;
        }
        if (StringUtils.isNoneBlank(e.getErrorCode())) {
            String msgCode = MSG_ERR_CODE_PREFIX + e.getErrorCode();
            String msg = messageSource.getMessage(msgCode, e.getArgs(), locale);
            return msg;
        } else {
            return e.getMessage();
        }
    }

}
