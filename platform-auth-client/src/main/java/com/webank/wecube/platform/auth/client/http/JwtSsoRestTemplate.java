package com.webank.wecube.platform.auth.client.http;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.auth.client.context.JwtSsoClientContext;
import com.webank.wecube.platform.auth.client.model.JwtSsoAccessToken;

/**
 * 
 * @author gavin
 *
 */
public class JwtSsoRestTemplate extends RestTemplate implements JwtSsoRestOperations {
    private static final Logger log = LoggerFactory.getLogger(JwtSsoRestTemplate.class);

    private JwtSsoClientContext jwtSsoClientContext;

    public JwtSsoRestTemplate(JwtSsoClientContext jwtSsoClientContext) {
        super();
        this.jwtSsoClientContext = jwtSsoClientContext;
        setErrorHandler(new JwtSsoResponseErrorHandler());
    }

    @Override
    public void setErrorHandler(ResponseErrorHandler errorHandler) {
        if (!(errorHandler instanceof JwtSsoResponseErrorHandler)) {
            errorHandler = new JwtSsoResponseErrorHandler(errorHandler);
        }

        super.setErrorHandler(errorHandler);
    }

    @Override
    protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {

        ClientHttpRequest req = super.createRequest(url, method);
        JwtSsoAccessToken at = jwtSsoClientContext.getAccessToken();
        if (log.isDebugEnabled()) {
            log.debug("request {} with access token:{}", url.toString(), at.getToken());
        }
        req.getHeaders().add(JwtSsoClientContext.HEADER_AUTHORIZATION,
                JwtSsoClientContext.PREFIX_BEARER_TOKEN + at.getToken());
        return req;
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
            ResponseExtractor<T> responseExtractor) throws RestClientException {
        try {
            return super.doExecute(url, method, requestCallback, responseExtractor);
        } catch (JwtSsoAccessTokenRequiredException e) {
            if (log.isInfoEnabled()) {
                log.info("access token is invalid and try again.");
            }
            jwtSsoClientContext.refreshToken();
            return super.doExecute(url, method, requestCallback, responseExtractor);
        }
    }

}
