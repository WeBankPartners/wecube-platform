package com.webank.wecube.platform.core.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    public static final String ACCESS_LOGGER = "access";
    
    private static final Logger log = LoggerFactory.getLogger(AccessLogInterceptor.class);

    private static final Logger accessLog = LoggerFactory.getLogger(ACCESS_LOGGER);
    
    private static final String ATTR_KEY_ACCESS_LOG = "wecube_attr_access_log";
    
    private ObjectMapper objectMapper = new ObjectMapper();

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        AccessLog alog = buildAccessLog(request);
        request.setAttribute(ATTR_KEY_ACCESS_LOG, alog);
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        AccessLog alog = (AccessLog) request.getAttribute(ATTR_KEY_ACCESS_LOG);
        if(alog == null){
            return;
        }
        
        alog.setEndTime(System.currentTimeMillis());
        alog.setResponseStatus(String.valueOf(response.getStatus()));
        accessLog.info("{}", alog);
    }
    
    private AccessLog buildAccessLog(HttpServletRequest request){
        AccessLog alog = new AccessLog();
        alog.setId(UUID.randomUUID().toString().replace("-", ""));
        alog.setPath(request.getRequestURI());
        alog.setRemoteAddr(request.getRemoteAddr());
        alog.setHttpMethod(request.getMethod());
        String requestData = "";
        try {
            requestData = objectMapper.writeValueAsString(request.getParameterMap());
        } catch (JsonProcessingException e) {
            log.info("errors occurred", e);
        }
        alog.setRequestData(requestData);
        alog.setStartTime(System.currentTimeMillis());
        
        String username = "";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null){
            username = auth.getName();
        }
        alog.setUsername(username);
        
        return alog;
    }
}
