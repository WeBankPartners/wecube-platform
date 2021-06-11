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
            List<Map<String, Object>> parameters, String requestId, List<String> allowedOptions, String dueDate) {
        PluginRequest<Map<String, Object>> requestObj = new DefaultPluginRequest().withInputs(parameters)
                .withRequestId(requestId);
        if (allowedOptions != null && !allowedOptions.isEmpty()) {
            requestObj = requestObj.withAllowedOptions(allowedOptions);
        }
        requestObj = requestObj.withDueDate(dueDate);

        return doCallPluginService(asPluginServerUrl(instanceAddress, path), requestObj);
    }

    public PluginResponse<Object> callPluginService(String instanceAddress, String path,
            List<Map<String, Object>> parameters, String requestId) {
        return doCallPluginService(asPluginServerUrl(instanceAddress, path),
                new DefaultPluginRequest().withInputs(parameters).withRequestId(requestId));
    }

    protected PluginResponse<Object> doCallPluginService(String targetUrl, PluginRequest<?> parameters) {
        log.debug("About to call {} with parameters: {} ", targetUrl, parameters);
        if(parameters.getInputs() == null || parameters.getInputs().isEmpty()){
            log.debug("Inputs is empty so returned immediately.");
            PluginResponse<Object> emptyResponse = new  DefaultPluginResponse();
            emptyResponse.setResultCode(PluginResponse.RESULT_CODE_OK);
            emptyResponse.setResultMessage("Empty inputs.");
            return emptyResponse;
        }
        PluginResponse<Object> response = jwtSsoRestTemplate.postForObject(targetUrl, parameters,
                DefaultPluginResponse.class);
        log.debug("Plugin response: {} ", response);

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
