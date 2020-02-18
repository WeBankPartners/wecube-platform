package com.webank.wecube.platform.core.support.plugin;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.platform.core.support.RestClient;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginRequest.DefaultPluginRequest;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse;
import com.webank.wecube.platform.core.support.plugin.dto.PluginResponse.DefaultPluginResponse;

@Service("pluginInvocationRestClient")
public class PluginInvocationRestClient implements RestClient {
    private static final Logger log = LoggerFactory.getLogger(PluginInvocationRestClient.class);
    
    @Autowired
    @Qualifier(value = "jwtSsoRestTemplate")
    private RestTemplate jwtSsoRestTemplate;
    
    
    public PluginResponse<Object> callPluginService(String instanceAddress, String path,
            List<Map<String, Object>> parameters, String requestId) {
        return doCallPluginService(asPluginServerUrl(instanceAddress, path),
                new DefaultPluginRequest().withInputs(parameters).withRequestId(requestId));
    }
    
    protected PluginResponse<Object> doCallPluginService(String targetUrl, PluginRequest<?> parameters) {
        log.info("About to call {} with parameters: {} ", targetUrl, parameters);
        PluginResponse<Object> response = jwtSsoRestTemplate.postForObject(targetUrl, parameters,
                DefaultPluginResponse.class);
        log.info("Plugin response: {} ", response);

        return response;
    }
    
    protected String asPluginServerUrl(String instanceAddress, String originPath, Object... pathVariables) {
        String solvedPath = originPath;
        if (pathVariables != null && pathVariables.length > 0) {
            solvedPath = String.format(originPath, pathVariables);
        }
        return "http://" + instanceAddress + solvedPath;
    }

}
