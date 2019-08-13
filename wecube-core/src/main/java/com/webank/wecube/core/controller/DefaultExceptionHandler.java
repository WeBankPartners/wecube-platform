package com.webank.wecube.core.controller;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.support.RemoteCallException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.webank.wecube.core.domain.JsonResponse.*;

@Slf4j
@ControllerAdvice
public class DefaultExceptionHandler {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public JsonResponse handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        if(exception instanceof RemoteCallException) {
            RemoteCallException remoteCallException = (RemoteCallException) exception;
            return error(remoteCallException.getErrorMessage()).withData(remoteCallException.getErrorData());
        }
        else {
            return error(exception.getMessage());
        }
    }


}
