package com.webank.wecube.platform.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "platform.core.msg.errorcode")
@PropertySource("classpath:message/message.properties")
@Component
public class ErrorMessageProperties {
    private String errorCode;
    private String errorMeaage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMeaage() {
        return errorMeaage;
    }

    public void setErrorMeaage(String errorMeaage) {
        this.errorMeaage = errorMeaage;
    }
}
