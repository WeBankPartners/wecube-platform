package com.webank.wecube.platform.core.interceptor;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;

@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (AuthenticationContextHolder.getCurrentUser() != null) {
            HttpHeaders headers = request.getHeaders();
            headers.add("Authorization", AuthenticationContextHolder.getCurrentUser().getAuthorization());
        }
        return execution.execute(request, body);
    }
}