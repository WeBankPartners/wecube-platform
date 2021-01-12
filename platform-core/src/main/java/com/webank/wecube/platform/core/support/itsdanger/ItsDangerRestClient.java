package com.webank.wecube.platform.core.support.itsdanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.http.UserJwtSsoTokenRestTemplate;
import com.webank.wecube.platform.core.support.RestClient;
import com.webank.wecube.platform.core.support.RestClientException;

@Service
public class ItsDangerRestClient implements RestClient {
    private static final Logger log = LoggerFactory.getLogger(ItsDangerRestClient.class);

    private static final String ITS_DANGER_CHECK_API_PATH = "/itsdangerous/v1/batch_execution_detection";

    @Autowired
    @Qualifier("userJwtSsoTokenRestTemplate")
    protected UserJwtSsoTokenRestTemplate userJwtSsoTokenRestTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;

    public ItsDangerCheckRespDto check(ItsDangerCheckReqDto requestDto) {
        String url = String.format("http://%s%s", applicationProperties.getGatewayUrl(), ITS_DANGER_CHECK_API_PATH);

        log.info("About to post {} {}", url, requestDto);

        ItsDangerCheckRespDto resp = userJwtSsoTokenRestTemplate.postForObject(url, requestDto,
                ItsDangerCheckRespDto.class);

        if ("OK".equalsIgnoreCase(resp.getStatus())) {
            return resp;
        } else {
            throw new RestClientException(resp.getStatus(),
                    "Error got for dangerous commands checking:" + resp.getCode() + "," + resp.getMessage());
        }

    }

}
