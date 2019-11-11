package com.webank.wecube.platform.auth.client.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import com.webank.wecube.platform.auth.client.context.JwtSsoClientContext;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoResponseErrorHandler implements ResponseErrorHandler {
    private final ResponseErrorHandler errorHandler;

    public JwtSsoResponseErrorHandler() {
        this.errorHandler = new DefaultResponseErrorHandler();
    }

    public JwtSsoResponseErrorHandler(ResponseErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return HttpStatus.Series.CLIENT_ERROR.equals(response.getStatusCode().series())
                || this.errorHandler.hasError(response);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (!HttpStatus.Series.CLIENT_ERROR.equals(response.getStatusCode().series())) {
            errorHandler.handleError(response);
        } else {
            ClientHttpResponse bufferedResponse = new ClientHttpResponse() {
                private byte[] lazyBody;

                public HttpStatus getStatusCode() throws IOException {
                    return response.getStatusCode();
                }

                public synchronized InputStream getBody() throws IOException {
                    if (lazyBody == null) {
                        InputStream bodyStream = response.getBody();
                        if (bodyStream != null) {
                            lazyBody = FileCopyUtils.copyToByteArray(bodyStream);
                        } else {
                            lazyBody = new byte[0];
                        }
                    }
                    return new ByteArrayInputStream(lazyBody);
                }

                public HttpHeaders getHeaders() {
                    return response.getHeaders();
                }

                public String getStatusText() throws IOException {
                    return response.getStatusText();
                }

                public void close() {
                    response.close();
                }

                public int getRawStatusCode() throws IOException {
                    return this.getStatusCode().value();
                }
            };

            List<String> authenticateHeaders = bufferedResponse.getHeaders()
                    .get(JwtSsoClientContext.HEADER_WWW_AUTHENTICATE);
            if (authenticateHeaders != null) {
                for (String authenticateHeader : authenticateHeaders) {
                    if (authenticateHeader.startsWith(JwtSsoClientContext.PREFIX_BEARER_TOKEN)) {
                        throw new JwtSsoAccessTokenRequiredException("access token required.");
                    }
                }
            }
            errorHandler.handleError(bufferedResponse);
        }

    }

}
