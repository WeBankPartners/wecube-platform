package com.webank.wecube.platform.core.commons;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class HttpRequestErrorHandler implements ResponseErrorHandler {


    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        boolean hasError = false;
        int rawStatusCode = clientHttpResponse.getRawStatusCode();
        if (rawStatusCode != 200) {
            hasError = true;
        }
        return hasError;
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

    }
}
